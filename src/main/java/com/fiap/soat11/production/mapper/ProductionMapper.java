package com.fiap.soat11.production.mapper;

import java.util.UUID;

import com.fiap.soat11.production.config.ProductionConstants;
import com.fiap.soat11.production.dto.ConsumeDTO;
import com.fiap.soat11.production.entity.Customer;
import com.fiap.soat11.production.entity.OrderItem;
import com.fiap.soat11.production.entity.Production;
import com.fiap.soat11.production.exception.ProductionException;

public class ProductionMapper {

    public static Production toProduction(ConsumeDTO consumeDTO) {
        validateConsumeDTO(consumeDTO);
        
        Production production = new Production();
        production.setId(UUID.randomUUID().toString());
        production.setOrderID(consumeDTO.getPayload().getId().toString());
        production.setStatus(ProductionConstants.PRODUCTION_STATUS_RECEIVED);
        production.setCustomer(new Customer(consumeDTO.getPayload().getCustomer().getName()));
        production.setItems(consumeDTO.getPayload().getItens().stream()
                .map(item -> new OrderItem(item.getName(), item.getQuantity()))
                .toList());

        return production;
    }

    private static void validateConsumeDTO(ConsumeDTO consumeDTO) {
        if (consumeDTO == null) {
            throw new ProductionException("ConsumeDTO cannot be null");
        }
        
        if (consumeDTO.getPayload() == null) {
            throw new ProductionException("Payload cannot be null");
        }
        
        if (consumeDTO.getPayload().getId() == null) {
            throw new ProductionException("Order ID cannot be null");
        }
        
        if (consumeDTO.getPayload().getCustomer() == null || 
            consumeDTO.getPayload().getCustomer().getName() == null) {
            throw new ProductionException("Customer data is invalid");
        }
        
        if (consumeDTO.getPayload().getItens() == null || consumeDTO.getPayload().getItens().isEmpty()) {
            throw new ProductionException("Items cannot be empty");
        }
    }
}
