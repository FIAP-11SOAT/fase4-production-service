package com.example.production.dto.request;

import com.example.production.enums.ProductionStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateProductionStatusRequest {
    
    @NotNull(message = "Status é obrigatório")
    private ProductionStatus status;
    
    private String reason;
}