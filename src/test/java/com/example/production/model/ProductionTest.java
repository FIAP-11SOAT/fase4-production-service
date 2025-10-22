package com.example.production.model;

import com.example.production.enums.ProductionStatus;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class ProductionTest {

    @Test
    void shouldCreateProductionWithBuilder() {
        // Given
        Long orderId = 123L;
        var productIds = Arrays.asList(1L, 2L, 3L);
        Instant now = Instant.now();

        // When
        Production production = Production.builder()
                .orderId(orderId)
                .productIds(productIds)
                .status(ProductionStatus.PREPARING)
                .startedAt(now)
                .build();

        // Then
        assertNotNull(production);
        assertEquals(orderId, production.getOrderId());
        assertEquals(productIds, production.getProductIds());
        assertEquals(ProductionStatus.PREPARING, production.getStatus());
        assertEquals(now, production.getStartedAt());
    }

    @Test
    void shouldUpdateStatus() {
        // Given
        Production production = Production.builder()
                .orderId(123L)
                .status(ProductionStatus.PREPARING)
                .build();

        // When
        production.updateStatus(ProductionStatus.IN_PROGRESS);

        // Then
        assertEquals(ProductionStatus.IN_PROGRESS, production.getStatus());
    }

    @Test
    void shouldCompleteProduction() {
        // Given
        Production production = Production.builder()
                .orderId(123L)
                .status(ProductionStatus.IN_PROGRESS)
                .build();

        // When
        production.updateStatus(ProductionStatus.DONE);

        // Then
        assertEquals(ProductionStatus.DONE, production.getStatus());
        assertNotNull(production.getFinishedAt());
    }

    @Test
    void shouldThrowExceptionForInvalidTransition() {
        // Given
        Production production = Production.builder()
                .orderId(123L)
                .status(ProductionStatus.DONE)
                .build();

        // When & Then
        assertThrows(com.example.production.exception.InvalidStatusTransitionException.class, 
            () -> production.updateStatus(ProductionStatus.IN_PROGRESS));
    }
}