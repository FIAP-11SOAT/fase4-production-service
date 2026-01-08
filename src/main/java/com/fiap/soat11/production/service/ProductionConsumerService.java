package com.fiap.soat11.production.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.fiap.soat11.production.dto.ConsumeDTO;
import com.fiap.soat11.production.entity.Production;
import com.fiap.soat11.production.exception.ProductionException;
import com.fiap.soat11.production.mapper.ProductionMapper;

import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;

@Service
public class ProductionConsumerService {
    
    private static final Logger logger = LoggerFactory.getLogger(ProductionConsumerService.class);
    
    private final DynamoDbTable<Production> dynamoDBClient;

    public ProductionConsumerService(DynamoDbTable<Production> dynamoDBClient) {
        this.dynamoDBClient = dynamoDBClient;
    }
    
    public void handle(ConsumeDTO message) {
        try {
            logger.info("Processing message with order ID: {}", 
                message.getPayload().getId());
            
            Production production = ProductionMapper.toProduction(message);
            
            dynamoDBClient.putItem(production);
            
            logger.info("Production record created successfully with ID: {}", 
                production.getId());
            
        } catch (ProductionException e) {
            logger.error("Validation error while processing production: {}", 
                e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error while processing production: {}", 
                e.getMessage(), e);
            throw new ProductionException("Error processing production order", e);
        }
    }
}
