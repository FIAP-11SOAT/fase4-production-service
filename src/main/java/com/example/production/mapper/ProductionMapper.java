package com.example.production.mapper;

import com.example.production.dto.request.CreateProductionRequest;
import com.example.production.dto.response.ProductionResponse;
import com.example.production.enums.ProductionStatus;
import com.example.production.model.Production;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ProductionMapper {

    public Production toEntity(CreateProductionRequest request) {
        return Production.builder()
                .orderId(request.getOrderId())
                .productIds(request.getProductIds())
                .status(ProductionStatus.NEW)
                .startedAt(Instant.now())
                .build();
    }

    public ProductionResponse toResponse(Production entity) {
        return ProductionResponse.builder()
                .id(entity.getId())
                .orderId(entity.getOrderId())
                .productIds(entity.getProductIds())
                .status(entity.getStatus())
                .startedAt(entity.getStartedAt())
                .finishedAt(entity.getFinishedAt())
                .build();
    }

    public List<ProductionResponse> toResponseList(List<Production> entities) {
        return entities.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}