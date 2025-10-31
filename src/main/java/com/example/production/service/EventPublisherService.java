package com.example.production.service;

import com.example.production.dto.event.OutgoingEvent;
import com.example.production.dto.event.payload.ProductionEventPayload;
import com.example.production.model.Production;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventPublisherService {

    private final SqsClient sqsClient;
    private final ObjectMapper objectMapper;
    
    // Esta URL deveria vir do Parameter Store ou configuração
    private static final String ORDER_SERVICE_QUEUE_URL = "https://sqs.us-east-1.amazonaws.com/814147156565/order-service-queue";

    public void publishProductionStartedEvent(Production production) {
        try {
            ProductionEventPayload payload = ProductionEventPayload.builder()
                    .orderId(production.getOrderId())
                    .productionId(production.getId())
                    .status(production.getStatus())
                    .startedAt(production.getStartedAt())
                    .estimatedTime(production.getEstimatedTime())
                    .build();

            OutgoingEvent event = OutgoingEvent.createProductionEvent(
                    "production-started-event",
                    objectMapper.convertValue(payload, new TypeReference<Map<String, Object>>() {})
            );

            publishEvent(event);
            log.info("Published production-started-event for order: {}", production.getOrderId());
            
        } catch (Exception e) {
            log.error("Failed to publish production-started-event for order: {}", production.getOrderId(), e);
        }
    }

    public void publishProductionCompletedEvent(Production production) {
        try {
            ProductionEventPayload payload = ProductionEventPayload.builder()
                    .orderId(production.getOrderId())
                    .productionId(production.getId())
                    .status(production.getStatus())
                    .startedAt(production.getStartedAt())
                    .completedAt(production.getCompletedAt())
                    .build();

            OutgoingEvent event = OutgoingEvent.createProductionEvent(
                    "production-completed-event",
                    objectMapper.convertValue(payload, new TypeReference<Map<String, Object>>() {})
            );

            publishEvent(event);
            log.info("Published production-completed-event for order: {}", production.getOrderId());
            
        } catch (Exception e) {
            log.error("Failed to publish production-completed-event for order: {}", production.getOrderId(), e);
        }
    }

    private void publishEvent(OutgoingEvent event) throws JsonProcessingException {
        String json = objectMapper.writeValueAsString(event);
        
        SendMessageRequest request = SendMessageRequest.builder()
                .queueUrl(ORDER_SERVICE_QUEUE_URL)
                .messageBody(json)
                .build();

        sqsClient.sendMessage(request);
        log.debug("Event published to order-service-queue: {}", event.getMeta().getEventName());
    }
}