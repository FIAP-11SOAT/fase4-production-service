package com.example.production.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateProductionRequest {
    
    @NotNull(message = "Order ID é obrigatório")
    @Positive(message = "Order ID deve ser positivo")
    private Long orderId;
    
    @NotEmpty(message = "Lista de produtos não pode estar vazia")
    private List<@Positive(message = "Product ID deve ser positivo") Long> productIds;
}