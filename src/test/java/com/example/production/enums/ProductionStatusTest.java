package com.example.production.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProductionStatusTest {

    @Test
    void shouldContainExpectedValues() {
        ProductionStatus[] statuses = ProductionStatus.values();
        
        assertTrue(statuses.length > 0, "ProductionStatus should have at least one value");
        
        // Test that we can get enum by name
        for (ProductionStatus status : statuses) {
            assertEquals(status, ProductionStatus.valueOf(status.name()));
        }
    }
}