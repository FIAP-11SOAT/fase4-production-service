package com.fiap.soat11.production.dto;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ProductionMessageDTOTest {

    private MetaDTO meta;
    private PayloadWrapperDTO payload;

    @BeforeEach
    void setUp() {
        meta = new MetaDTO();
        meta.setEventId("event-123");
        meta.setEventDate("2026-01-07T02:50:33.250572100Z");
        meta.setEventSource("production-service");
        meta.setEventTarget("order-service");
        meta.setEventName("production-completed-event");

        ProductionPayloadDTO productionPayload = new ProductionPayloadDTO("order-123");
        payload = new PayloadWrapperDTO(productionPayload);
    }

    @Test
    void testDefaultConstructor() {
        // Act
        ProductionMessageDTO dto = new ProductionMessageDTO();

        // Assert
        assertNotNull(dto);
        assertNull(dto.getMeta());
        assertNull(dto.getPayload());
    }

    @Test
    void testConstructorWithParameters() {
        // Act
        ProductionMessageDTO dto = new ProductionMessageDTO(meta, payload);

        // Assert
        assertNotNull(dto);
        assertNotNull(dto.getMeta());
        assertNotNull(dto.getPayload());
        assertEquals("event-123", dto.getMeta().getEventId());
        assertEquals("order-123", dto.getPayload().getProduction().getOrderId());
    }

    @Test
    void testSetterAndGetter() {
        // Arrange
        ProductionMessageDTO dto = new ProductionMessageDTO();

        // Act
        dto.setMeta(meta);
        dto.setPayload(payload);

        // Assert
        assertNotNull(dto.getMeta());
        assertNotNull(dto.getPayload());
        assertEquals("production-service", dto.getMeta().getEventSource());
        assertEquals("order-service", dto.getMeta().getEventTarget());
    }

    @Test
    void testEqualsAndHashCode() {
        // Arrange
        ProductionMessageDTO dto1 = new ProductionMessageDTO(meta, payload);
        
        MetaDTO meta2 = new MetaDTO();
        meta2.setEventId("event-123");
        meta2.setEventDate("2026-01-07T02:50:33.250572100Z");
        meta2.setEventSource("production-service");
        meta2.setEventTarget("order-service");
        meta2.setEventName("production-completed-event");
        
        ProductionPayloadDTO productionPayload2 = new ProductionPayloadDTO("order-123");
        PayloadWrapperDTO payload2 = new PayloadWrapperDTO(productionPayload2);
        ProductionMessageDTO dto2 = new ProductionMessageDTO(meta2, payload2);

        ProductionPayloadDTO productionPayload3 = new ProductionPayloadDTO("order-456");
        PayloadWrapperDTO payload3 = new PayloadWrapperDTO(productionPayload3);
        ProductionMessageDTO dto3 = new ProductionMessageDTO(meta, payload3);

        // Assert
        assertEquals(dto1, dto2);
        assertNotEquals(dto1, dto3);
        assertEquals(dto1.hashCode(), dto2.hashCode());
    }

    @Test
    void testToString() {
        // Arrange
        ProductionMessageDTO dto = new ProductionMessageDTO(meta, payload);

        // Act
        String toString = dto.toString();

        // Assert
        assertNotNull(toString);
        assertTrue(toString.contains("meta"));
        assertTrue(toString.contains("payload"));
    }

    @Test
    void testSetMetaWithNull() {
        // Arrange
        ProductionMessageDTO dto = new ProductionMessageDTO(meta, payload);

        // Act
        dto.setMeta(null);

        // Assert
        assertNull(dto.getMeta());
        assertNotNull(dto.getPayload());
    }

    @Test
    void testSetPayloadWithNull() {
        // Arrange
        ProductionMessageDTO dto = new ProductionMessageDTO(meta, payload);

        // Act
        dto.setPayload(null);

        // Assert
        assertNotNull(dto.getMeta());
        assertNull(dto.getPayload());
    }

    @Test
    void testCompleteMessageStructure() {
        // Arrange
        MetaDTO metaDTO = new MetaDTO(
            "1541bb12-c15b-4491-ae8d-d54b056d81b8",
            "2026-01-07T02:50:33.250572100Z",
            "production-service",
            "order-service",
            "production-completed-event"
        );

        ProductionPayloadDTO productionPayloadDTO = new ProductionPayloadDTO("f47ac10b-58cc-4372-a567-0e02b2c3d479");
        PayloadWrapperDTO payloadWrapperDTO = new PayloadWrapperDTO(productionPayloadDTO);

        // Act
        ProductionMessageDTO message = new ProductionMessageDTO(metaDTO, payloadWrapperDTO);

        // Assert
        assertNotNull(message);
        assertNotNull(message.getMeta());
        assertNotNull(message.getPayload());
        assertEquals("1541bb12-c15b-4491-ae8d-d54b056d81b8", message.getMeta().getEventId());
        assertEquals("production-completed-event", message.getMeta().getEventName());
        assertEquals("f47ac10b-58cc-4372-a567-0e02b2c3d479", 
                     message.getPayload().getProduction().getOrderId());
    }

    @Test
    void testMessageWithStartedEvent() {
        // Arrange
        MetaDTO startedMeta = new MetaDTO(
            "event-456",
            "2026-01-07T03:00:00.000Z",
            "production-service",
            "order-service",
            "production-started-event"
        );

        ProductionPayloadDTO productionPayload = new ProductionPayloadDTO("order-789");
        PayloadWrapperDTO wrapper = new PayloadWrapperDTO(productionPayload);

        // Act
        ProductionMessageDTO message = new ProductionMessageDTO(startedMeta, wrapper);

        // Assert
        assertNotNull(message);
        assertEquals("production-started-event", message.getMeta().getEventName());
        assertEquals("order-789", message.getPayload().getProduction().getOrderId());
    }
}
