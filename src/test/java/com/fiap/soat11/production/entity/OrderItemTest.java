package com.fiap.soat11.production.entity;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class OrderItemTest {

    @Test
    void testOrderItemCreation() {
        // Arrange & Act
        OrderItem item = new OrderItem();
        item.setName("Product ABC");
        item.setQuantity(5);

        // Assert
        assertEquals("Product ABC", item.getName());
        assertEquals(5, item.getQuantity());
    }

    @Test
    void testOrderItemAllArgsConstructor() {
        // Act
        OrderItem item = new OrderItem("Product XYZ", 10);

        // Assert
        assertEquals("Product XYZ", item.getName());
        assertEquals(10, item.getQuantity());
    }

    @Test
    void testOrderItemNoArgsConstructor() {
        // Act
        OrderItem item = new OrderItem();

        // Assert
        assertNull(item.getName());
        assertNull(item.getQuantity());
    }

    @Test
    void testOrderItemUpdateName() {
        // Arrange
        OrderItem item = new OrderItem();
        item.setName("Product A");

        // Act
        item.setName("Product B");

        // Assert
        assertEquals("Product B", item.getName());
    }

    @Test
    void testOrderItemUpdateQuantity() {
        // Arrange
        OrderItem item = new OrderItem();
        item.setQuantity(5);

        // Act
        item.setQuantity(10);

        // Assert
        assertEquals(10, item.getQuantity());
    }

    @Test
    void testOrderItemZeroQuantity() {
        // Arrange & Act
        OrderItem item = new OrderItem("Product", 0);

        // Assert
        assertEquals(0, item.getQuantity());
    }

    @Test
    void testOrderItemLargeQuantity() {
        // Arrange & Act
        OrderItem item = new OrderItem("Product", 1000000);

        // Assert
        assertEquals(1000000, item.getQuantity());
    }

    @Test
    void testOrderItemNegativeQuantity() {
        // Arrange & Act
        OrderItem item = new OrderItem("Product", -5);

        // Assert
        assertEquals(-5, item.getQuantity());
    }

    @Test
    void testOrderItemMultipleUpdates() {
        // Arrange
        OrderItem item = new OrderItem();

        // Act & Assert
        for (int i = 1; i <= 5; i++) {
            item.setName("Product " + i);
            item.setQuantity(i);
            assertEquals("Product " + i, item.getName());
            assertEquals(i, item.getQuantity());
        }
    }
}

