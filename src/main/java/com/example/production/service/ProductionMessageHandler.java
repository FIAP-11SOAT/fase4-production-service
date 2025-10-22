package com.example.production.service;

import com.example.production.config.properties.SqsProperties;
import com.example.production.enums.ProductionStatus;
import com.example.production.exception.MessageProcessingException;
import com.example.production.model.Production;
import com.example.production.repository.ProductionRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductionMessageHandler {

    private final SqsClient sqsClient;
    private final ProductionRepository repository;
    private final SqsProperties sqsProperties;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostConstruct
    public void startListening() {
        Thread t = new Thread(this::listenMessages);
        t.setDaemon(true);
        t.start();
        log.info("Started SQS message listener");
    }

    private void listenMessages() {
        while (true) {
            try {
                ReceiveMessageRequest request = ReceiveMessageRequest.builder()
                        .queueUrl(sqsProperties.getOrderQueueUrl())
                        .maxNumberOfMessages(sqsProperties.getMaxNumberOfMessages())
                        .waitTimeSeconds(sqsProperties.getWaitTimeSeconds())
                        .visibilityTimeout(sqsProperties.getVisibilityTimeoutSeconds())
                        .build();

                List<Message> messages = sqsClient.receiveMessage(request).messages();

                for (Message message : messages) {
                    try {
                        handleOrderMessage(message.body());
                        deleteMessage(message);
                        log.debug("Successfully processed message: {}", message.messageId());
                    } catch (Exception e) {
                        log.error("Error processing message: {}", message.messageId(), e);
                        // optionally send to DLQ based on configuration
                    }
                }

                Thread.sleep(1000);
            } catch (Exception ex) {
                log.error("Error in message listening loop", ex);
                try { Thread.sleep(5000); } catch (InterruptedException ignored) {}
            }
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

    private void handleOrderMessage(String body) {
        try {
            // Parse JSON message
            JsonNode node = objectMapper.readTree(body);
            Long orderId = extractOrderId(node);
            List<Long> productIds = extractProductIds(node);
            
            // Check if production already exists
            if (repository.existsByOrderId(orderId)) {
                log.warn("Production already exists for order: {}", orderId);
                return;
            }

            // Create new production
            Production production = Production.builder()
                    .orderId(orderId)
                    .productIds(productIds)
                    .status(ProductionStatus.PREPARING)
                    .startedAt(Instant.now())
                    .build();
            
            repository.save(production);
            log.info("Created production for order: {} with {} products", orderId, productIds.size());
            
        } catch (Exception e) {
            throw new MessageProcessingException("Failed to process order message", body, e);
        }
    }

    private Long extractOrderId(JsonNode node) {
        if (!node.has("orderId")) {
            throw new IllegalArgumentException("Missing orderId in message");
        }
        return node.get("orderId").asLong();
    }

    private List<Long> extractProductIds(JsonNode node) {
        List<Long> productIds = new ArrayList<>();
        if (node.has("productIds") && node.get("productIds").isArray()) {
            for (JsonNode p : node.get("productIds")) {
                productIds.add(p.asLong());
            }
        }
        
        if (productIds.isEmpty()) {
            throw new IllegalArgumentException("Empty productIds in message");
        }
        
        return productIds;
    }

    public void completeProduction(Long orderId) {
        Production production = repository.findByOrderId(orderId)
                .orElseThrow(() -> new com.example.production.exception.ProductionNotFoundException("order-" + orderId));

        production.updateStatus(ProductionStatus.DONE);
        repository.save(production);

        publishProductionCompleted(orderId);
        log.info("Completed production for order: {}", orderId);
    }

    private void publishProductionCompleted(Long orderId) {
        try {
            String json = objectMapper.writeValueAsString(
                    java.util.Map.of(
                            "orderId", orderId, 
                            "status", ProductionStatus.DONE.getCode(),
                            "completedAt", Instant.now().toString()
                    ));
            
            sqsClient.sendMessage(SendMessageRequest.builder()
                    .queueUrl(sqsProperties.getProductionCompletedQueueUrl())
                    .messageBody(json)
                    .build());
            
            log.info("Published production completed message for order: {}", orderId);
        } catch (Exception e) {
            log.error("Failed to publish production completed message for order: {}", orderId, e);
            throw new MessageProcessingException("Failed to publish completion message", orderId.toString(), e);
        }
    }
}
