package com.fiap.soat11.production.entity;

import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fiap.soat11.production.config.TableName;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

@Data
@NoArgsConstructor
@AllArgsConstructor
@DynamoDbBean
@TableName("fase4-production-service-table")
public class Production {
    
    private String id;
    
    @JsonProperty("order_id")
    private String orderID;

    private String status;

    private Customer customer;

    private List<OrderItem> items;
    
    @DynamoDbPartitionKey
    public String getId() {
        return id;
    }
}