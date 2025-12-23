package com.fiap.soat11.production.entity;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

class ProductionTest {

    @Test
    void testProductionCreation() {
        // Arrange & Act
        Production production = new Production();
        production.setId("prod-123");
        production.setOrderID("order-456");
        production.setStatus("RECEIVED");
        production.setUpdatedAt(System.currentTimeMillis());

        // Assert
        assertEquals("prod-123", production.getId());
        assertEquals("order-456", production.getOrderID());
        assertEquals("RECEIVED", production.getStatus());
        assertNotNull(production.getUpdatedAt());
    }

    @Test
    void testProductionWithCustomer() {
        // Arrange
        Production production = new Production();
        Customer customer = new Customer();
        customer.setName("John Doe");

        // Act
        production.setCustomer(customer);

        // Assert
        assertNotNull(production.getCustomer());
        assertEquals("John Doe", production.getCustomer().getName());
    }

    @Test
    void testProductionWithItems() {
        // Arrange
        Production production = new Production();
        OrderItem item1 = new OrderItem();
        item1.setName("Item 1");
        item1.setQuantity(2);

        OrderItem item2 = new OrderItem();
        item2.setName("Item 2");
        item2.setQuantity(3);

        // Act
        production.setItems(Arrays.asList(item1, item2));

        // Assert
        assertNotNull(production.getItems());
        assertEquals(2, production.getItems().size());
        assertEquals("Item 1", production.getItems().get(0).getName());
    }

    @Test
    void testProductionEmptyItems() {
        // Arrange & Act
        Production production = new Production();
        production.setItems(Arrays.asList());

        // Assert
        assertNotNull(production.getItems());
        assertTrue(production.getItems().isEmpty());
    }

    @Test
    void testProductionStatusTransitions() {
        // Arrange
        Production production = new Production();
        String[] statuses = {"RECEIVED", "PREPARING", "READY", "FINISHED", "CANCELLED"};

        // Act & Assert
        for (String status : statuses) {
            production.setStatus(status);
            assertEquals(status, production.getStatus());
        }
    }

    @Test
    void testProductionAllArgsConstructor() {
        // Arrange
        Customer customer = new Customer("Jane Doe");
        
        OrderItem item = new OrderItem();
        item.setName("Test Item");

        List<OrderItem> items = Arrays.asList(item);
        Long timestamp = System.currentTimeMillis();

        // Act
        Production production = new Production("prod-123", "order-789", "PREPARING", timestamp, customer, items);

        // Assert
        assertEquals("prod-123", production.getId());
        assertEquals("order-789", production.getOrderID());
        assertEquals("PREPARING", production.getStatus());
        assertEquals(timestamp, production.getUpdatedAt());
        assertEquals("Jane Doe", production.getCustomer().getName());
        assertEquals(1, production.getItems().size());
    }

    @Test
    void testProductionNoArgsConstructor() {
        // Act
        Production production = new Production();

        // Assert
        assertNull(production.getId());
        assertNull(production.getOrderID());
        assertNull(production.getStatus());
        assertNull(production.getUpdatedAt());
        assertNull(production.getCustomer());
        assertNull(production.getItems());
    }

    @Test
    void testProductionUpdateMultipleFields() {
        // Arrange
        Production production = new Production();

        // Act
        production.setId("prod-999");
        production.setOrderID("order-999");
        production.setStatus("READY");
        
        // Assert
        assertEquals("prod-999", production.getId());
        assertEquals("order-999", production.getOrderID());
        assertEquals("READY", production.getStatus());
    }

    @Test
    void testProductionWithNullCustomer() {
        // Arrange & Act
        Production production = new Production();
        production.setCustomer(null);

        // Assert
        assertNull(production.getCustomer());
    }

    @Test
    void testProductionWithNullItems() {
        // Arrange & Act
        Production production = new Production();
        production.setItems(null);

        // Assert
        assertNull(production.getItems());
    }
}

