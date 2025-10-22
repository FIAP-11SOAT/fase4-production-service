package com.example.production.controller;

import com.example.production.dto.request.UpdateProductionStatusRequest;
import com.example.production.dto.response.ProductionResponse;
import com.example.production.enums.ProductionStatus;
import com.example.production.exception.ProductionNotFoundException;
import com.example.production.mapper.ProductionMapper;
import com.example.production.model.Production;
import com.example.production.repository.ProductionRepository;
import com.example.production.service.ProductionMessageHandler;
import com.example.production.config.properties.SqsProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/productions")
@RequiredArgsConstructor
@Slf4j
public class ProductionController {

    private final ProductionRepository repository;
    private final ProductionMessageHandler handler;
    private final ProductionMapper mapper;
    private final SqsClient sqsClient;
    private final SqsProperties sqsProperties;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @GetMapping
    public ResponseEntity<Page<ProductionResponse>> getAll(Pageable pageable) {
        log.debug("Fetching all productions with pagination: {}", pageable);
        
        Page<Production> productionsPage = repository.findAll(pageable);
        List<ProductionResponse> responseList = mapper.toResponseList(productionsPage.getContent());
        
        Page<ProductionResponse> responsePage = new PageImpl<>(
            responseList, 
            pageable, 
            productionsPage.getTotalElements()
        );
        
        return ResponseEntity.ok(responsePage);
    }

    @GetMapping("/stats/count-by-status")
    public ResponseEntity<Object> getCountByStatus() {
        log.debug("Fetching production count by status");
        
        var stats = new java.util.HashMap<String, Long>();
        for (ProductionStatus status : ProductionStatus.values()) {
            stats.put(status.name(), repository.countByStatus(status));
        }
        
        return ResponseEntity.ok(stats);
    }

    @PostMapping("/process-queue")
    public ResponseEntity<String> processQueueMessages(@RequestParam(defaultValue = "10") int maxMessages) {
        log.info("Processing queue messages, max messages: {}", maxMessages);
        
        try {
            ReceiveMessageRequest request = ReceiveMessageRequest.builder()
                    .queueUrl(sqsProperties.getOrderQueueUrl())
                    .maxNumberOfMessages(Math.min(maxMessages, 10))
                    .waitTimeSeconds(5)
                    .visibilityTimeout(30)
                    .build();

            List<Message> messages = sqsClient.receiveMessage(request).messages();
            int processedCount = 0;
            
            for (Message message : messages) {
                try {
                    processOrderMessage(message.body());
                    deleteMessage(message);
                    processedCount++;
                    log.debug("Successfully processed message: {}", message.messageId());
                } catch (Exception e) {
                    log.error("Error processing message: {}", message.messageId(), e);
                }
            }
            
            return ResponseEntity.ok(String.format("Processed %d messages from queue", processedCount));
        } catch (Exception e) {
            log.error("Error processing queue messages", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error processing queue: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/status-change")
    public ResponseEntity<String> publishStatusChange(
            @PathVariable String id,
            @Valid @RequestBody UpdateProductionStatusRequest request) {
        
        log.info("Publishing status change for production {} to status: {}", id, request.getStatus());
        
        try {
            Production production = repository.findById(id)
                    .orElseThrow(() -> new ProductionNotFoundException(id));
            
            // Update production status
            production.updateStatus(request.getStatus());
            Production updated = repository.save(production);
            
            // Publish status change to queue
            String json = objectMapper.writeValueAsString(
                    java.util.Map.of(
                            "productionId", updated.getId(),
                            "orderId", updated.getOrderId(),
                            "oldStatus", production.getStatus().getCode(),
                            "newStatus", request.getStatus().getCode(),
                            "updatedAt", Instant.now().toString()
                    ));
            
            sqsClient.sendMessage(SendMessageRequest.builder()
                    .queueUrl(sqsProperties.getProductionCompletedQueueUrl())
                    .messageBody(json)
                    .build());
            
            log.info("Published status change message for production: {}", id);
            return ResponseEntity.ok(String.format("Status updated to %s and message published", request.getStatus()));
            
        } catch (ProductionNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error publishing status change for production: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error publishing status change: " + e.getMessage());
        }
    }

    private void processOrderMessage(String body) {
        try {
            var node = objectMapper.readTree(body);
            Long orderId = node.get("orderId").asLong();
            
            if (repository.existsByOrderId(orderId)) {
                log.warn("Production already exists for order: {}", orderId);
                return;
            }

            var productIds = new java.util.ArrayList<Long>();
            if (node.has("productIds") && node.get("productIds").isArray()) {
                for (var p : node.get("productIds")) {
                    productIds.add(p.asLong());
                }
            }
            
            Production production = Production.builder()
                    .orderId(orderId)
                    .productIds(productIds)
                    .status(ProductionStatus.PREPARING)
                    .startedAt(Instant.now())
                    .build();
            
            repository.save(production);
            log.info("Created production for order: {} with {} products", orderId, productIds.size());
            
        } catch (Exception e) {
            log.error("Failed to process order message: {}", body, e);
            throw new RuntimeException("Failed to process order message", e);
        }
    }

    private void deleteMessage(Message message) {
        try {
            sqsClient.deleteMessage(DeleteMessageRequest.builder()
                    .queueUrl(sqsProperties.getOrderQueueUrl())
                    .receiptHandle(message.receiptHandle())
                    .build());
        } catch (Exception e) {
            log.error("Failed to delete message: {}", message.messageId(), e);
        }
    }
}
