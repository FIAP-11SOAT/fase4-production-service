package com.example.production.service;

import com.example.production.config.properties.SqsProperties;
import com.example.production.dto.event.IncomingEvent;
import com.example.production.dto.event.payload.PaymentCompletedPayload;
import com.example.production.enums.ProductionStatus;
import com.example.production.exception.MessageProcessingException;
import com.example.production.model.Production;
import com.example.production.repository.ProductionRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class EnhancedProductionMessageHandler {

    private final SqsClient sqsClient;
    private final SqsProperties sqsProperties;
    private final ProductionRepository repository;
    private final EventPublisherService eventPublisher;
    private final ObjectMapper objectMapper;

    @EventListener(ApplicationReadyEvent.class)
    public void startListening() {
        new Thread(this::listenMessages).start();
        log.info("Started enhanced SQS message listener");
    }

    private void listenMessages() {
        while (true) {
            try {
                ReceiveMessageRequest request = ReceiveMessageRequest.builder()
                        .queueUrl(sqsProperties.getAwsSqsOrderQueueUrl())
                        .maxNumberOfMessages(sqsProperties.getAwsSqsMaxNumberOfMessages())
                        .waitTimeSeconds(sqsProperties.getAwsSqsWaitTimeSeconds())
                        .visibilityTimeout(sqsProperties.getAwsSqsVisibilityTimeoutSeconds())
                        .build();

                List<Message> messages = sqsClient.receiveMessage(request).messages();

                for (Message message : messages) {
                    try {
                        handleMessage(message.body());
                        deleteMessage(message);
                        log.debug("Successfully processed message: {}", message.messageId());
                    } catch (Exception e) {
                        log.error("Error processing message: {}", message.messageId(), e);
                    }
                }

                Thread.sleep(1000);
            } catch (Exception ex) {
                log.error("Error in message listener loop", ex);
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }

    private void handleMessage(String body) {
        try {
            // Try to parse as new event format
            IncomingEvent event = objectMapper.readValue(body, IncomingEvent.class);
            
            if (event.getMeta() != null && "payment-completed".equals(event.getMeta().getEventName())) {
                handlePaymentCompletedEvent(event);
            } else {
                log.warn("Received unknown event type: {}", 
                    event.getMeta() != null ? event.getMeta().getEventName() : "null");
            }
        } catch (Exception e) {
            // Fallback to old format for backward compatibility
            try {
                handleOrderMessage(body);
            } catch (Exception fallbackError) {
                log.error("Failed to process message in both formats", fallbackError);
                throw new MessageProcessingException("Failed to process message", body, fallbackError);
            }
        }
    }

    private void handlePaymentCompletedEvent(IncomingEvent event) {
        try {
            Map<String, Object> payloadMap = event.getPayload();
            PaymentCompletedPayload payload = objectMapper.convertValue(
                payloadMap, PaymentCompletedPayload.class);

            Long orderId = payload.getOrderId();
            
            // Check if production already exists
            if (repository.existsByOrderId(orderId)) {
                log.warn("Production already exists for order: {}", orderId);
                return;
            }

            // Extract product IDs from items
            List<Long> productIds = payload.getItems().stream()
                    .map(PaymentCompletedPayload.ProductionItem::getProductId)
                    .toList();

            // Create new production
            Production production = Production.builder()
                    .id(UUID.randomUUID().toString())
                    .orderId(orderId)
                    .productIds(productIds)
                    .status(ProductionStatus.PENDING)
                    .startedAt(Instant.now())
                    .estimatedTime(calculateEstimatedTime(productIds))
                    .build();
            
            production.setCreatedAtIfNull();

            // Save to DynamoDB
            repository.save(production);
            log.info("Created production for order: {} with status: {}", orderId, production.getStatus());

            // Update status to PREPARING and publish event
            production.updateStatus(ProductionStatus.PREPARING);
            repository.save(production);
            
            // Publish production started event
            eventPublisher.publishProductionStartedEvent(production);
            
        } catch (Exception e) {
            log.error("Failed to process payment completed event", e);
            throw new MessageProcessingException("Failed to process payment completed event", 
                event.toString(), e);
        }
    }

    // Fallback method for old format (backward compatibility)
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
                    .id(UUID.randomUUID().toString())
                    .orderId(orderId)
                    .productIds(productIds)
                    .status(ProductionStatus.PENDING)
                    .startedAt(Instant.now())
                    .estimatedTime(calculateEstimatedTime(productIds))
                    .build();
            
            production.setCreatedAtIfNull();

            // Save to DynamoDB
            repository.save(production);
            log.info("Created production for order: {} with status: {}", orderId, production.getStatus());

            // Publish production started event
            eventPublisher.publishProductionStartedEvent(production);
            
        } catch (Exception e) {
            log.error("Failed to process order message", e);
            throw new MessageProcessingException("Failed to process order message", body, e);
        }
    }

    private Long extractOrderId(JsonNode node) {
        if (node.has("orderId")) {
            return node.get("orderId").asLong();
        }
        throw new IllegalArgumentException("Order ID not found in message");
    }

    private List<Long> extractProductIds(JsonNode node) {
        if (node.has("items") && node.get("items").isArray()) {
            return node.get("items")
                    .findValuesAsText("productId")
                    .stream()
                    .map(Long::parseLong)
                    .toList();
        }
        return List.of();
    }

    private Integer calculateEstimatedTime(List<Long> productIds) {
        // Simple estimation: 5 minutes per product
        return productIds.size() * 5;
    }

    private void deleteMessage(Message message) {
        try {
            DeleteMessageRequest deleteRequest = DeleteMessageRequest.builder()
                    .queueUrl(sqsProperties.getAwsSqsOrderQueueUrl())
                    .receiptHandle(message.receiptHandle())
                    .build();

            sqsClient.deleteMessage(deleteRequest);
            log.debug("Deleted message: {}", message.messageId());
        } catch (Exception e) {
            log.error("Failed to delete message: {}", message.messageId(), e);
        }
    }
}