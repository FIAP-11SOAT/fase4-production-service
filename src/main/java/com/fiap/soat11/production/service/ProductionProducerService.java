package com.fiap.soat11.production.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.fiap.soat11.production.config.ProductionConstants;
import com.fiap.soat11.production.dto.MetaDTO;
import com.fiap.soat11.production.dto.PayloadWrapperDTO;
import com.fiap.soat11.production.dto.ProductionMessageDTO;
import com.fiap.soat11.production.dto.ProductionPayloadDTO;
import com.fiap.soat11.production.entity.Production;
import com.fiap.soat11.production.exception.ProductionException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.awspring.cloud.sqs.operations.SqsTemplate;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import java.time.Instant;

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
            logger.debug("Iniciando atualização de status para Production");
            
            // Buscar a Production atual pelo ID
            Key key = Key.builder().partitionValue(productionId).build();
            Production production = dynamoDBClient.getItem(key);
            
            if (production == null) {
                logger.error("Production não encontrada");
                throw new ProductionException("Production não encontrada com ID: " + productionId);
            }
            
            // Atualizar o status e o timestamp
            production.setStatus(status);
            production.setUpdatedAt(System.currentTimeMillis());
            
            // Salvar no DynamoDB
            dynamoDBClient.putItem(production);
            logger.debug("Production atualizada no DynamoDB");
            
            // Publicar apenas o status na fila SQS
            publishStatusMessage(production);
            logger.debug("Status publicado na fila SQS");
            
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
     * Publica o Meta com o eventName correspondente ao status na fila SQS
     * 
     * @param production Production com o status a ser publicado
     */
    private void publishStatusMessage(Production production) {
        try {
            // Criar o MetaDTO com o eventName correspondente ao status
            MetaDTO meta = createMetaFromStatus(production.getStatus());
            
            // Criar o ProductionPayloadDTO com o order_id
            ProductionPayloadDTO productionPayload = new ProductionPayloadDTO(production.getOrderID());
            
            // Criar o PayloadWrapperDTO
            PayloadWrapperDTO payloadWrapper = new PayloadWrapperDTO(productionPayload);
            
            // Criar a mensagem completa com meta e payload
            ProductionMessageDTO message = new ProductionMessageDTO(meta, payloadWrapper);
            
            // Converter para JSON
            String jsonMessage = objectMapper.writeValueAsString(message);
            
            // Log da mensagem que será enviada
            System.out.println("=== MENSAGEM ENVIADA PARA SQS ===");
            System.out.println(jsonMessage);
            System.out.println("=================================");
            
            // Enviar para a fila SQS
            sqsTemplate.send(ProductionConstants.SQS_QUEUE_PRODUCER, jsonMessage);
            
            logger.debug("Meta enviado para SQS com conteúdo: {}", jsonMessage);
        } catch (Exception ex) {
            logger.error("Erro ao publicar status na fila: {}", ex.getMessage(), ex);
            throw new ProductionException("Erro ao publicar status na fila: " + ex.getMessage(), ex);
        }
    }
    
    /**
     * Cria um MetaDTO baseado no status fornecido
     * 
     * @param status O status da production
     * @return MetaDTO com eventName correspondente
     */
    private MetaDTO createMetaFromStatus(String status) {
        String eventName = mapStatusToEventName(status);
        
        MetaDTO meta = new MetaDTO();
        meta.setEventId(java.util.UUID.randomUUID().toString());
        meta.setEventDate(Instant.now().toString());
        meta.setEventSource("production-service");
        meta.setEventTarget("order-service");
        meta.setEventName(eventName);
        
        return meta;
    }
    
    /**
     * Mapeia o status para o eventName correspondente
     * 
     * @param status O status da production
     * @return O eventName correspondente
     */
    private String mapStatusToEventName(String status) {
        if (status == null) {
            throw new ProductionException("Status não pode ser nulo");
        }
        
        return switch (status.toUpperCase()) {
            case "STARTED" -> "production-started-event";
            case "COMPLETED" -> "production-completed-event";
            default -> throw new ProductionException("Status inválido: " + status);
        };
    }
    
    public void sendProductionMessage(String message) {
        // Logic to send message to the production queue
    }
}
