package com.example.production.exception;

import lombok.Getter;

@Getter
public class ProductionNotFoundException extends RuntimeException {
    
    private final String productionId;
    
    public ProductionNotFoundException(String productionId) {
        super("Produção não encontrada com ID: " + productionId);
        this.productionId = productionId;
    }
}