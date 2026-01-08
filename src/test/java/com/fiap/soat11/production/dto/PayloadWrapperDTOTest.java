package com.fiap.soat11.production.dto;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class PayloadWrapperDTOTest {

    @Test
    void testDefaultConstructor() {
        // Act
        PayloadWrapperDTO dto = new PayloadWrapperDTO();

        // Assert
        assertNotNull(dto);
        assertNull(dto.getProduction());
    }

    @Test
    void testConstructorWithProduction() {
        // Arrange
        ProductionPayloadDTO productionPayload = new ProductionPayloadDTO("order-123");

        // Act
        PayloadWrapperDTO dto = new PayloadWrapperDTO(productionPayload);

        // Assert
        assertNotNull(dto);
        assertNotNull(dto.getProduction());
        assertEquals("order-123", dto.getProduction().getOrderId());
    }

    @Test
    void testSetterAndGetter() {
        // Arrange
        PayloadWrapperDTO dto = new PayloadWrapperDTO();
        ProductionPayloadDTO productionPayload = new ProductionPayloadDTO("test-order-456");

        // Act
        dto.setProduction(productionPayload);

        // Assert
        assertNotNull(dto.getProduction());
        assertEquals("test-order-456", dto.getProduction().getOrderId());
    }

    @Test
    void testEqualsAndHashCode() {
        // Arrange
        ProductionPayloadDTO payload1 = new ProductionPayloadDTO("order-123");
        ProductionPayloadDTO payload2 = new ProductionPayloadDTO("order-123");
        ProductionPayloadDTO payload3 = new ProductionPayloadDTO("order-456");

        PayloadWrapperDTO dto1 = new PayloadWrapperDTO(payload1);
        PayloadWrapperDTO dto2 = new PayloadWrapperDTO(payload2);
        PayloadWrapperDTO dto3 = new PayloadWrapperDTO(payload3);

        // Assert
        assertEquals(dto1, dto2);
        assertNotEquals(dto1, dto3);
        assertEquals(dto1.hashCode(), dto2.hashCode());
    }

    @Test
    void testToString() {
        // Arrange
        ProductionPayloadDTO productionPayload = new ProductionPayloadDTO("order-789");
        PayloadWrapperDTO dto = new PayloadWrapperDTO(productionPayload);

        // Act
        String toString = dto.toString();

        // Assert
        assertNotNull(toString);
        assertTrue(toString.contains("production"));
    }

    @Test
    void testSetProductionWithNull() {
        // Arrange
        ProductionPayloadDTO productionPayload = new ProductionPayloadDTO("order-123");
        PayloadWrapperDTO dto = new PayloadWrapperDTO(productionPayload);

        // Act
        dto.setProduction(null);

        // Assert
        assertNull(dto.getProduction());
    }

    @Test
    void testCompleteWorkflow() {
        // Arrange
        String orderId = "f47ac10b-58cc-4372-a567-0e02b2c3d479";
        ProductionPayloadDTO productionPayload = new ProductionPayloadDTO(orderId);

        // Act
        PayloadWrapperDTO wrapper = new PayloadWrapperDTO(productionPayload);

        // Assert
        assertNotNull(wrapper);
        assertNotNull(wrapper.getProduction());
        assertEquals(orderId, wrapper.getProduction().getOrderId());
    }
}
