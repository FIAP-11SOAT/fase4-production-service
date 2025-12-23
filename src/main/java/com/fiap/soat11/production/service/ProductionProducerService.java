package com.fiap.soat11.production.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.fiap.soat11.production.config.ProductionConstants;
import com.fiap.soat11.production.entity.Production;
import com.fiap.soat11.production.exception.ProductionException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.awspring.cloud.sqs.operations.SqsTemplate;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;

@Service
public class ProductionProducerService {

    private static final Logger logger = LoggerFactory.getLogger(ProductionProducerService.class);

    private final DynamoDbTable<Production> dynamoDBClient;
    private final SqsTemplate sqsTemplate;
    private final ObjectMapper objectMapper;
    
    public ProductionProducerService(DynamoDbTable<Production> dynamoDBClient, 
                                     SqsTemplate sqsTemplate, 
                                     ObjectMapper objectMapper) {
        this.dynamoDBClient = dynamoDBClient;
        this.sqsTemplate = sqsTemplate;
        this.objectMapper = objectMapper;
    }
    
    /**
     * Atualiza o status e updatedAt de uma Production no DynamoDB e publica o status na fila SQS
     * 
     * @param productionId ID da Production a ser atualizada
     * @param status Novo status da Production
     * @return A Production atualizada
     * @throws ProductionException se a Production não for encontrada
     */
    public Production updateStatusAndPublish(String productionId, String status) {
        try {
            logger.info("Iniciando atualização de status para Production: {} com novo status: {}", 
                       productionId, status);
            
            // Buscar a Production atual pelo ID
            Key key = Key.builder().partitionValue(productionId).build();
            Production production = dynamoDBClient.getItem(key);
            
            if (production == null) {
                logger.error("Production não encontrada com ID: {}", productionId);
                throw new ProductionException("Production não encontrada com ID: " + productionId);
            }
            
            // Atualizar o status e o timestamp
            production.setStatus(status);
            production.setUpdatedAt(System.currentTimeMillis());
            
            // Salvar no DynamoDB
            dynamoDBClient.putItem(production);
            logger.info("Production atualizada no DynamoDB: {} com status: {}", productionId, status);
            
            // Publicar apenas o status na fila SQS
            publishStatusMessage(production);
            logger.info("Status publicado na fila SQS para Production: {}", productionId);
            
            return production;
        } catch (ProductionException ex) {
            logger.error("Erro ao atualizar status da Production: {}", ex.getMessage());
            throw ex;
        } catch (Exception ex) {
            logger.error("Erro inesperado ao atualizar status da Production: {}", ex.getMessage(), ex);
            throw new ProductionException("Erro ao atualizar status da Production: " + ex.getMessage(), ex);
        }
    }
    
    /**
     * Publica apenas o status na fila SQS
     * 
     * @param production Production com o status a ser publicado
     */
    private void publishStatusMessage(Production production) {
        try {
            // Criar um mapa com apenas o status
            java.util.Map<String, Object> payload = new java.util.HashMap<>();
            payload.put("status", production.getStatus());
            
            // Converter para JSON
            String jsonMessage = objectMapper.writeValueAsString(payload);
            
            // Enviar para a fila SQS
            sqsTemplate.send(ProductionConstants.SQS_QUEUE_PRODUCER, jsonMessage);
            
            logger.debug("Status enviado para SQS com conteúdo: {}", jsonMessage);
        } catch (Exception ex) {
            logger.error("Erro ao publicar status na fila: {}", ex.getMessage(), ex);
            throw new ProductionException("Erro ao publicar status na fila: " + ex.getMessage(), ex);
        }
    }
    
    public void sendProductionMessage(String message) {
        // Logic to send message to the production queue
    }
}
