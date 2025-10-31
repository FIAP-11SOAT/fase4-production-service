package com.example.production.dto.event.payload;

import com.example.production.enums.ProductionStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductionEventPayload {
    
    @JsonProperty("orderId")
    private Long orderId;
    
    @JsonProperty("productionId")
    private String productionId;
    
    private ProductionStatus status;
    
    @JsonProperty("startedAt")
    private Instant startedAt;
    
    @JsonProperty("completedAt")
    private Instant completedAt;
    
    @JsonProperty("estimatedTime")
    private Integer estimatedTime;
}