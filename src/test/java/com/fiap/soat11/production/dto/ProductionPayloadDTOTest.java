package com.fiap.soat11.production.dto;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ProductionPayloadDTOTest {

    @Test
    void testDefaultConstructor() {
        // Act
        ProductionPayloadDTO dto = new ProductionPayloadDTO();

        // Assert
        assertNotNull(dto);
        assertNull(dto.getOrderId());
    }

    @Test
    void testConstructorWithOrderId() {
        // Arrange
        String orderId = "f47ac10b-58cc-4372-a567-0e02b2c3d479";

        // Act
        ProductionPayloadDTO dto = new ProductionPayloadDTO(orderId);

        // Assert
        assertNotNull(dto);
        assertEquals(orderId, dto.getOrderId());
    }

    @Test
    void testSetterAndGetter() {
        // Arrange
        ProductionPayloadDTO dto = new ProductionPayloadDTO();
        String orderId = "test-order-123";

        // Act
        dto.setOrderId(orderId);

        // Assert
        assertEquals(orderId, dto.getOrderId());
    }

    @Test
    void testEqualsAndHashCode() {
        // Arrange
        String orderId = "order-123";
        ProductionPayloadDTO dto1 = new ProductionPayloadDTO(orderId);
        ProductionPayloadDTO dto2 = new ProductionPayloadDTO(orderId);
        ProductionPayloadDTO dto3 = new ProductionPayloadDTO("different-order");

        // Assert
        assertEquals(dto1, dto2);
        assertNotEquals(dto1, dto3);
        assertEquals(dto1.hashCode(), dto2.hashCode());
    }

    @Test
    void testToString() {
        // Arrange
        String orderId = "order-123";
        ProductionPayloadDTO dto = new ProductionPayloadDTO(orderId);

        // Act
        String toString = dto.toString();

        // Assert
        assertNotNull(toString);
        assertTrue(toString.contains("order-123"));
    }

    @Test
    void testSetOrderIdWithNull() {
        // Arrange
        ProductionPayloadDTO dto = new ProductionPayloadDTO("initial-order");

        // Act
        dto.setOrderId(null);

        // Assert
        assertNull(dto.getOrderId());
    }
}
