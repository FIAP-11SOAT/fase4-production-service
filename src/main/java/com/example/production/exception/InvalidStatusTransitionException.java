package com.example.production.exception;

import com.example.production.enums.ProductionStatus;
import lombok.Getter;

@Getter
public class InvalidStatusTransitionException extends RuntimeException {
    
    private final ProductionStatus currentStatus;
    private final ProductionStatus targetStatus;
    
    public InvalidStatusTransitionException(ProductionStatus currentStatus, ProductionStatus targetStatus) {
        super(String.format("Transição inválida de status: %s -> %s", 
                currentStatus.getDescription(), targetStatus.getDescription()));
        this.currentStatus = currentStatus;
        this.targetStatus = targetStatus;
    }
}