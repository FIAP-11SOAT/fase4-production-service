package com.example.production.dto.event.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCompletedPayload {
    
    @JsonProperty("orderId")
    private Long orderId;
    
    private List<ProductionItem> items;
    
    private String status;
    
    @JsonProperty("startedAt")
    private Instant startedAt;
    
    @JsonProperty("completedAt")
    private Instant completedAt;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductionItem {
        @JsonProperty("productId")
        private Long productId;
        
        private String name;
        
        private Integer quantity;
    }
}