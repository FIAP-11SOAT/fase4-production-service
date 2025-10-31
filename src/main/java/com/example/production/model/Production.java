package com.example.production.model;

import com.example.production.enums.ProductionStatus;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondarySortKey;

import java.time.Instant;
import java.util.List;

@DynamoDbBean
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Production {

    private String id;
    private Long orderId;
    private List<Long> productIds;
    private ProductionStatus status;
    private Instant startedAt;
    private Instant finishedAt;
    private Instant completedAt;
    private Integer estimatedTime; // em minutos
    private String createdAt; // For DynamoDB GSI sorting
    
    @DynamoDbPartitionKey
    public String getId() {
        return id;
    }
    
    @DynamoDbSecondaryPartitionKey(indexNames = "OrderIdIndex")
    public Long getOrderId() {
        return orderId;
    }
    
    @DynamoDbSecondaryPartitionKey(indexNames = "StatusIndex")
    public ProductionStatus getStatus() {
        return status;
    }
    
    @DynamoDbSecondarySortKey(indexNames = "StatusIndex")
    public String getCreatedAt() {
        return createdAt;
    }
    
    public void updateStatus(ProductionStatus newStatus) {
        if (this.status != null && !this.status.canTransitionTo(newStatus)) {
            throw new com.example.production.exception.InvalidStatusTransitionException(this.status, newStatus);
        }
        this.status = newStatus;
        
        if (newStatus.isCompleted() && this.finishedAt == null) {
            this.finishedAt = Instant.now();
            this.completedAt = Instant.now();
        }
    }
    
    // Método para definir automaticamente createdAt quando necessário
    public void setCreatedAtIfNull() {
        if (this.createdAt == null) {
            this.createdAt = Instant.now().toString();
        }
    }
}
