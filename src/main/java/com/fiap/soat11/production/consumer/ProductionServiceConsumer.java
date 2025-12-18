package com.fiap.soat11.production.consumer;

import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.fiap.soat11.production.dto.ConsumeDTO;
import com.fiap.soat11.production.entity.Production;

import io.awspring.cloud.sqs.annotation.SqsListener;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

@Service
public class ProductionServiceConsumer {


    private final DynamoDbTable<Production> dynamoDBClient;
    
    public ProductionServiceConsumer(DynamoDbEnhancedClient client) {
        this.dynamoDBClient = client.table(
            "fase4-production-service-table",
            TableSchema.fromBean(Production.class)
        );
    }

    @SqsListener("fase4-production-service-queue")
    public void listen(ConsumeDTO message) {

        System.out.println(message);

        Production production = new Production();
        production.setId(UUID.randomUUID().toString());
        production.setOrderID(message.getPayload().getId().toString());
        production.setStatus("RECEIVED");
        production.setCustomer(new com.fiap.soat11.production.entity.Customer(message.getPayload().getCustomer().getName()));
        production.setItems(message.getPayload().getItens().stream().map(
            item -> new com.fiap.soat11.production.entity.OrderItem(item.getName(), item.getQuantity())
        ).collect(Collectors.toList()));

        dynamoDBClient.putItem(production);
    }

}
