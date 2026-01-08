package com.fiap.soat11.production.mapper;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.fiap.soat11.production.config.ProductionConstants;
import com.fiap.soat11.production.dto.ConsumeDTO;
import com.fiap.soat11.production.dto.CustomerDTO;
import com.fiap.soat11.production.dto.ItensDTO;
import com.fiap.soat11.production.dto.PayloadDTO;
import com.fiap.soat11.production.entity.Production;
import com.fiap.soat11.production.exception.ProductionException;

class ProductionMapperTest {

    @Test
    void testToProductionSuccess() {
        // Arrange
        ConsumeDTO consumeDTO = createValidConsumeDTO();

        // Act
        Production production = ProductionMapper.toProduction(consumeDTO);

        // Assert
        assertNotNull(production);
        assertNotNull(production.getId());
        assertEquals("123e4567-e89b-12d3-a456-426614174000", production.getOrderID());
        assertEquals(ProductionConstants.PRODUCTION_STATUS_RECEIVED, production.getStatus());
        assertEquals("John Doe", production.getCustomer().getName());
        assertEquals(2, production.getItems().size());
    }

    @Test
    void testToProductionWithNullConsumeDTO() {
        // Act & Assert
        assertThrows(ProductionException.class, () -> ProductionMapper.toProduction(null));
    }

    @Test
    void testToProductionWithNullPayload() {
        // Arrange
        ConsumeDTO consumeDTO = new ConsumeDTO();
        consumeDTO.setPayload(null);

        // Act & Assert
        assertThrows(ProductionException.class, () -> ProductionMapper.toProduction(consumeDTO));
    }

    @Test
    void testToProductionWithNullOrderId() {
        // Arrange
        ConsumeDTO consumeDTO = new ConsumeDTO();
        PayloadDTO payload = new PayloadDTO();
        payload.setId(null);
        consumeDTO.setPayload(payload);

        // Act & Assert
        assertThrows(ProductionException.class, () -> ProductionMapper.toProduction(consumeDTO));
    }

    @Test
    void testToProductionWithNullCustomer() {
        // Arrange
        ConsumeDTO consumeDTO = new ConsumeDTO();
        PayloadDTO payload = new PayloadDTO();
        payload.setId(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"));
        payload.setCustomer(null);
        consumeDTO.setPayload(payload);

        // Act & Assert
        assertThrows(ProductionException.class, () -> ProductionMapper.toProduction(consumeDTO));
    }

    @Test
    void testToProductionWithEmptyItems() {
        // Arrange
        ConsumeDTO consumeDTO = new ConsumeDTO();
        PayloadDTO payload = new PayloadDTO();
        payload.setId(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"));
        CustomerDTO customer = new CustomerDTO();
        customer.setName("John Doe");
        payload.setCustomer(customer);
        payload.setItens(Arrays.asList());
        consumeDTO.setPayload(payload);

        // Act & Assert
        assertThrows(ProductionException.class, () -> ProductionMapper.toProduction(consumeDTO));
    }

    private ConsumeDTO createValidConsumeDTO() {
        ConsumeDTO consumeDTO = new ConsumeDTO();
        
        PayloadDTO payload = new PayloadDTO();
        payload.setId(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"));
        
        CustomerDTO customer = new CustomerDTO();
        customer.setName("John Doe");
        payload.setCustomer(customer);
        
        ItensDTO item1 = new ItensDTO();
        item1.setName("Item 1");
        item1.setQuantity(5);
        
        ItensDTO item2 = new ItensDTO();
        item2.setName("Item 2");
        item2.setQuantity(3);
        
        payload.setItens(Arrays.asList(item1, item2));
        consumeDTO.setPayload(payload);
        
        return consumeDTO;
    }
}
