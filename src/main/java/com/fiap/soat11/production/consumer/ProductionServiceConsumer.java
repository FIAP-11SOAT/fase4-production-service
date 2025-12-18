package com.fiap.soat11.production.consumer;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fiap.soat11.production.dto.ConsumeDTO;
import com.fiap.soat11.production.entity.Production;

import io.awspring.cloud.dynamodb.DynamoDbTemplate;
import io.awspring.cloud.sqs.annotation.SqsListener;

@Service
public class ProductionServiceConsumer {

    @Autowired
    private DynamoDbTemplate dynamoDbTemplate;

    @SqsListener("fase4-production-service-queue")
    public void listen(ConsumeDTO message) {

        System.out.println(message);

        Production production = new Production();
        production.setId(UUID.randomUUID());
        production.setOrderID(message.getPayload().getId());
            production.setStatus("RECEIVED");
        production.setCustomer(new com.fiap.soat11.production.entity.Customer(message.getPayload().getCustomer().getName()));
        // production.setItems(message.getPayload().getItens().stream().map(
        //     item -> new com.fiap.soat11.production.entity.OrderItem(item.getName(), item.getQuantity())
        // ).toArray(com.fiap.soat11.production.entity.OrderItem[]::new));

        dynamoDbTemplate.save(production);
    }

}
