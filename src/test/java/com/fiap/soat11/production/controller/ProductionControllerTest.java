package com.fiap.soat11.production.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.fiap.soat11.production.dto.ErrorResponse;
import com.fiap.soat11.production.entity.Customer;
import com.fiap.soat11.production.entity.OrderItem;
import com.fiap.soat11.production.entity.Production;
import com.fiap.soat11.production.exception.ProductionException;
import com.fiap.soat11.production.service.ProductionProducerService;

@ExtendWith(MockitoExtension.class)
class ProductionControllerTest {

    @Mock
    private ProductionProducerService productionProducerService;

    private ProductionController controller;
    private Production testProduction;

    @BeforeEach
    void setUp() {
        controller = new ProductionController(productionProducerService);
        
        Customer customer = new Customer();
        customer.setName("John Doe");

        OrderItem item = new OrderItem();
        item.setName("Test Product");
        item.setQuantity(2);

        testProduction = new Production();
        testProduction.setId("prod-123");
        testProduction.setOrderID("order-123");
        testProduction.setStatus("RECEIVED");
        testProduction.setCustomer(customer);
        testProduction.setItems(java.util.Arrays.asList(item));
    }

    @Test
    void testUpdateProductionStatusToStartedSuccess() {
        // Arrange
        String productionId = "prod-123";
        testProduction.setStatus("STARTED");
        
        when(productionProducerService.updateStatusAndPublish(productionId, "STARTED"))
            .thenReturn(testProduction);

        // Act
        ResponseEntity<?> response = controller.updateProductionStatusToStarted(productionId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testProduction, response.getBody());
        verify(productionProducerService, times(1)).updateStatusAndPublish(productionId, "STARTED");
    }

    @Test
    void testUpdateProductionStatusToCompletedSuccess() {
        // Arrange
        String productionId = "prod-123";
        testProduction.setStatus("COMPLETED");
        
        when(productionProducerService.updateStatusAndPublish(productionId, "COMPLETED"))
            .thenReturn(testProduction);

        // Act
        ResponseEntity<?> response = controller.updateProductionStatusToCompleted(productionId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testProduction, response.getBody());
        verify(productionProducerService, times(1)).updateStatusAndPublish(productionId, "COMPLETED");
    }

    @Test
    void testUpdateProductionStatusToStartedNotFound() {
        // Arrange
        String productionId = "prod-123";
        
        when(productionProducerService.updateStatusAndPublish(productionId, "STARTED"))
            .thenThrow(new ProductionException("Production not found"));

        // Act
        ResponseEntity<?> response = controller.updateProductionStatusToStarted(productionId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertInstanceOf(ErrorResponse.class, response.getBody());
        ErrorResponse errorResponse = (ErrorResponse) response.getBody();
        assertEquals(404, errorResponse.getStatus());
        assertTrue(errorResponse.getMessage().contains("Production not found"));
    }

    @Test
    void testUpdateProductionStatusToCompletedNotFound() {
        // Arrange
        String productionId = "prod-123";
        
        when(productionProducerService.updateStatusAndPublish(productionId, "COMPLETED"))
            .thenThrow(new ProductionException("Production not found"));

        // Act
        ResponseEntity<?> response = controller.updateProductionStatusToCompleted(productionId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertInstanceOf(ErrorResponse.class, response.getBody());
        ErrorResponse errorResponse = (ErrorResponse) response.getBody();
        assertEquals(404, errorResponse.getStatus());
        assertTrue(errorResponse.getMessage().contains("Production not found"));
    }

    @Test
    void testUpdateProductionStatusToStartedGenericException() {
        // Arrange
        String productionId = "prod-123";
        
        when(productionProducerService.updateStatusAndPublish(productionId, "STARTED"))
            .thenThrow(new RuntimeException("Database error"));

        // Act
        ResponseEntity<?> response = controller.updateProductionStatusToStarted(productionId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertInstanceOf(ErrorResponse.class, response.getBody());
        ErrorResponse errorResponse = (ErrorResponse) response.getBody();
        assertEquals(500, errorResponse.getStatus());
        assertTrue(errorResponse.getMessage().contains("Database error"));
    }

    @Test
    void testUpdateProductionStatusToCompletedGenericException() {
        // Arrange
        String productionId = "prod-123";
        
        when(productionProducerService.updateStatusAndPublish(productionId, "COMPLETED"))
            .thenThrow(new RuntimeException("Database error"));

        // Act
        ResponseEntity<?> response = controller.updateProductionStatusToCompleted(productionId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertInstanceOf(ErrorResponse.class, response.getBody());
        ErrorResponse errorResponse = (ErrorResponse) response.getBody();
        assertEquals(500, errorResponse.getStatus());
        assertTrue(errorResponse.getMessage().contains("Database error"));
    }

    @Test
    void testListPendingProductionsSuccess() {
        // Arrange
        Production production1 = new Production();
        production1.setId("prod-1");
        production1.setStatus("RECEIVED");
        production1.setUpdatedAt(1000L);

        Production production2 = new Production();
        production2.setId("prod-2");
        production2.setStatus("IN_PROGRESS");
        production2.setUpdatedAt(2000L);

        java.util.List<Production> productionList = java.util.Arrays.asList(production1, production2);
        
        when(productionProducerService.listPendingProductions())
            .thenReturn(productionList);

        // Act
        ResponseEntity<?> response = controller.listPendingProductions();

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(productionList, response.getBody());
        verify(productionProducerService, times(1)).listPendingProductions();
    }

    @Test
    void testListPendingProductionsEmptyList() {
        // Arrange
        when(productionProducerService.listPendingProductions())
            .thenReturn(java.util.Collections.emptyList());

        // Act
        ResponseEntity<?> response = controller.listPendingProductions();

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertInstanceOf(java.util.List.class, response.getBody());
        assertTrue(((java.util.List<?>) response.getBody()).isEmpty());
        verify(productionProducerService, times(1)).listPendingProductions();
    }

    @Test
    void testListPendingProductionsProductionException() {
        // Arrange
        when(productionProducerService.listPendingProductions())
            .thenThrow(new ProductionException("Error listing productions"));

        // Act
        ResponseEntity<?> response = controller.listPendingProductions();

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertInstanceOf(ErrorResponse.class, response.getBody());
        ErrorResponse errorResponse = (ErrorResponse) response.getBody();
        assertEquals(500, errorResponse.getStatus());
        assertTrue(errorResponse.getMessage().contains("Error listing productions"));
    }

    @Test
    void testListPendingProductionsGenericException() {
        // Arrange
        when(productionProducerService.listPendingProductions())
            .thenThrow(new RuntimeException("Unexpected error"));

        // Act
        ResponseEntity<?> response = controller.listPendingProductions();

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertInstanceOf(ErrorResponse.class, response.getBody());
        ErrorResponse errorResponse = (ErrorResponse) response.getBody();
        assertEquals(500, errorResponse.getStatus());
        assertTrue(errorResponse.getMessage().contains("Unexpected error"));
    }
}
