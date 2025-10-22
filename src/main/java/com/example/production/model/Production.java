package com.example.production.model;

import com.example.production.enums.ProductionStatus;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Document(collection = "productions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Production {

    @Id
    private String id;
    private Long orderId;
    private List<Long> productIds;
    private ProductionStatus status;
    private Instant startedAt;
    private Instant finishedAt;
    
    public void updateStatus(ProductionStatus newStatus) {
        if (this.status != null && !this.status.canTransitionTo(newStatus)) {
            throw new com.example.production.exception.InvalidStatusTransitionException(this.status, newStatus);
        }
        this.status = newStatus;
        
        if (newStatus.isCompleted() && this.finishedAt == null) {
            this.finishedAt = Instant.now();
        }
    }
}
