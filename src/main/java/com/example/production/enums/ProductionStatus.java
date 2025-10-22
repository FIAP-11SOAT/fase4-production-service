package com.example.production.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ProductionStatus {
    PENDING("PENDING", "Aguardando processamento"),
    NEW("NEW", "Nova produção criada"),
    PREPARING("PREPARING", "Em preparação"),
    IN_PROGRESS("IN_PROGRESS", "Em andamento"),
    DONE("DONE", "Concluída"),
    ERROR("ERROR", "Erro na produção"),
    CANCELLED("CANCELLED", "Cancelada");

    private final String code;
    private final String description;

    public static ProductionStatus fromCode(String code) {
        for (ProductionStatus status : values()) {
            if (status.getCode().equalsIgnoreCase(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Status inválido: " + code);
    }

    public boolean isCompleted() {
        return this == DONE || this == ERROR || this == CANCELLED;
    }

    public boolean canTransitionTo(ProductionStatus newStatus) {
        return switch (this) {
            case PENDING -> newStatus == NEW || newStatus == PREPARING || newStatus == CANCELLED;
            case NEW -> newStatus == PREPARING || newStatus == CANCELLED;
            case PREPARING -> newStatus == IN_PROGRESS || newStatus == ERROR || newStatus == CANCELLED;
            case IN_PROGRESS -> newStatus == DONE || newStatus == ERROR || newStatus == CANCELLED;
            case DONE, ERROR, CANCELLED -> false; // Estados finais
        };
    }
}