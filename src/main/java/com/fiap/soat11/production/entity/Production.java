package com.fiap.soat11.production.entity;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

@Data
@NoArgsConstructor
@AllArgsConstructor
@DynamoDbBean
public class Production {
    
    private UUID id;
    
    @JsonProperty("order_id")
    private UUID orderID;

    private String status;

    private Customer customer;

    private OrderItem[] items;
    
    @DynamoDbPartitionKey
    public UUID getId() {
        return id;
    }
}