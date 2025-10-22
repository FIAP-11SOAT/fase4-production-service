package com.example.production.dto.response;

import com.example.production.enums.ProductionStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductionResponse {
    
    private String id;
    private Long orderId;
    private List<Long> productIds;
    private ProductionStatus status;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant startedAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant finishedAt;
    
    private Long durationInSeconds;
    
    // Método utilitário para calcular duração
    public Long getDurationInSeconds() {
        if (startedAt != null && finishedAt != null) {
            return finishedAt.getEpochSecond() - startedAt.getEpochSecond();
        }
        return null;
    }
}