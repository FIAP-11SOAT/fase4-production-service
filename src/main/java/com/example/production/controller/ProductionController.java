package com.example.production.controller;

import com.example.production.dto.request.UpdateProductionStatusRequest;
import com.example.production.dto.response.ProductionResponse;
import com.example.production.enums.ProductionStatus;
import com.example.production.exception.ProductionNotFoundException;
import com.example.production.mapper.ProductionMapper;
import com.example.production.model.Production;
import com.example.production.repository.ProductionRepository;
import com.example.production.service.ProductionMessageHandler;
import com.example.production.service.EventPublisherService;
import com.example.production.config.properties.SqsProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private final EventPublisherService eventPublisher;
    private final ObjectMapper objectMapper;

    @GetMapping
    public ResponseEntity<List<ProductionResponse>> getAll() {
        log.debug("Fetching all productions");
        
        List<Production> productions = repository.findAll();
        List<ProductionResponse> responseList = mapper.toResponseList(productions);
        
        return ResponseEntity.ok(responseList);
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
            int processedCount = processMessages(Math.min(maxMessages, 10));
            return ResponseEntity.ok(String.format("Processed %d messages from queue", processedCount));
        } catch (Exception e) {
            log.error("Error processing queue messages", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error processing queue: " + e.getMessage());
        }
    }

    private int processMessages(int maxMessages) {
        ReceiveMessageRequest request = ReceiveMessageRequest.builder()
                .queueUrl(sqsProperties.getAwsSqsOrderQueueUrl())
                .maxNumberOfMessages(maxMessages)
                .waitTimeSeconds(5)
                .visibilityTimeout(30)
                .build();

        List<Message> messages = sqsClient.receiveMessage(request).messages();
        int processedCount = 0;
        
        for (Message message : messages) {
            if (processAndDeleteMessage(message)) {
                processedCount++;
            }
        }
        
        return processedCount;
    }

    private boolean processAndDeleteMessage(Message message) {
        try {
            processOrderMessage(message.body());
            deleteMessage(message);
            log.debug("Successfully processed message: {}", message.messageId());
            return true;
        } catch (Exception e) {
            log.error("Error processing message: {}", message.messageId(), e);
            return false;
        }
    }

    @PostMapping("/{id}/status-change")
    public ResponseEntity<String> publishStatusChange(
            @PathVariable String id,
            @Valid @RequestBody UpdateProductionStatusRequest request) {
        
        log.info("Publishing status change for production {} to status: {}", id, request.getStatus());
        
        try {
            Production production = findProductionById(id);
            Production updated = updateProductionStatus(production, request.getStatus());
            publishStatusChangeMessage(updated, production.getStatus());
            
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

    private Production findProductionById(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new ProductionNotFoundException(id));
    }

    private Production updateProductionStatus(Production production, ProductionStatus newStatus) {
        production.updateStatus(newStatus);
        return repository.save(production);
    }

    private void publishStatusChangeMessage(Production updated, ProductionStatus oldStatus) throws Exception {
        // Use the EventPublisherService for structured event publishing
        if (updated.getStatus() == ProductionStatus.IN_PROGRESS) {
            eventPublisher.publishProductionStartedEvent(updated);
        } else if (updated.getStatus() == ProductionStatus.DONE) {
            eventPublisher.publishProductionCompletedEvent(updated);
        }
        
        log.info("Published status change event for production: {} from {} to {}", 
                updated.getId(), oldStatus, updated.getStatus());
    }

    private void processOrderMessage(String body) {
        try {
            var node = objectMapper.readTree(body);
            Long orderId = extractOrderId(node);
            
            if (repository.existsByOrderId(orderId)) {
                log.warn("Production already exists for order: {}", orderId);
                return;
            }

            var productIds = extractProductIds(node);
            Production production = createProduction(orderId, productIds);
            
            repository.save(production);
            log.info("Created production for order: {} with {} products", orderId, productIds.size());
            
        } catch (Exception e) {
            log.error("Failed to process order message: {}", body, e);
            throw new RuntimeException("Failed to process order message", e);
        }
    }

    private Long extractOrderId(com.fasterxml.jackson.databind.JsonNode node) {
        return node.get("orderId").asLong();
    }

    private java.util.List<Long> extractProductIds(com.fasterxml.jackson.databind.JsonNode node) {
        var productIds = new java.util.ArrayList<Long>();
        if (node.has("productIds") && node.get("productIds").isArray()) {
            for (var productNode : node.get("productIds")) {
                productIds.add(productNode.asLong());
            }
        }
        return productIds;
    }

    private Production createProduction(Long orderId, java.util.List<Long> productIds) {
        return Production.builder()
                .orderId(orderId)
                .productIds(productIds)
                .status(ProductionStatus.PREPARING)
                .startedAt(Instant.now())
                .build();
    }

    private void deleteMessage(Message message) {
        try {
            sqsClient.deleteMessage(DeleteMessageRequest.builder()
                    .queueUrl(sqsProperties.getAwsSqsOrderQueueUrl())
                    .receiptHandle(message.receiptHandle())
                    .build());
        } catch (Exception e) {
            log.error("Failed to delete message: {}", message.messageId(), e);
        }
    }
}
