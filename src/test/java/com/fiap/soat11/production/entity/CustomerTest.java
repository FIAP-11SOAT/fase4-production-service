package com.fiap.soat11.production.entity;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class CustomerTest {

    @Test
    void testCustomerCreation() {
        // Arrange & Act
        Customer customer = new Customer();
        customer.setName("John Doe");

        // Assert
        assertEquals("John Doe", customer.getName());
    }

    @Test
    void testCustomerAllArgsConstructor() {
        // Act
        Customer customer = new Customer("Jane Doe");

        // Assert
        assertEquals("Jane Doe", customer.getName());
    }

    @Test
    void testCustomerNoArgsConstructor() {
        // Act
        Customer customer = new Customer();

        // Assert
        assertNull(customer.getName());
    }

    @Test
    void testCustomerUpdateName() {
        // Arrange
        Customer customer = new Customer();
        customer.setName("John Doe");

        // Act
        customer.setName("Jane Doe");

        // Assert
        assertEquals("Jane Doe", customer.getName());
    }

    @Test
    void testCustomerWithSpecialCharactersInName() {
        // Arrange & Act
        Customer customer = new Customer("José María García");

        // Assert
        assertEquals("José María García", customer.getName());
    }

    @Test
    void testCustomerMultipleUpdates() {
        // Arrange
        Customer customer = new Customer();

        // Act & Assert
        for (int i = 1; i <= 5; i++) {
            customer.setName("Customer " + i);
            assertEquals("Customer " + i, customer.getName());
        }
    }

    @Test
    void testCustomerEquality() {
        // Arrange & Act
        Customer customer1 = new Customer("Test User");
        Customer customer2 = new Customer("Test User");

        // Assert
        assertEquals(customer1, customer2);
    }

    @Test
    void testCustomerInequality() {
        // Arrange & Act
        Customer customer1 = new Customer("User 1");
        Customer customer2 = new Customer("User 2");

        // Assert
        assertNotEquals(customer1, customer2);
    }
}

