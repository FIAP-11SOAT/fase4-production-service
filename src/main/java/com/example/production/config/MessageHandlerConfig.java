package com.example.production.config;

import com.example.production.service.EnhancedProductionMessageHandler;
import com.example.production.service.EventPublisherService;
import com.example.production.repository.ProductionRepository;
import com.example.production.config.properties.SqsProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import software.amazon.awssdk.services.sqs.SqsClient;

@Configuration
@EnableConfigurationProperties(SqsProperties.class)
@Slf4j
public class MessageHandlerConfig {

    @Bean
    @Primary
    public EnhancedProductionMessageHandler enhancedProductionMessageHandler(
            SqsClient sqsClient,
            SqsProperties sqsProperties,
            ProductionRepository repository,
            EventPublisherService eventPublisher,
            ObjectMapper objectMapper) {
        
        log.info("Creating EnhancedProductionMessageHandler with structured event support");
        return new EnhancedProductionMessageHandler(sqsClient, sqsProperties, repository, eventPublisher, objectMapper);
    }
}