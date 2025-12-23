package com.fiap.soat11.production.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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
    void testUpdateProductionStatusSuccess() {
        // Arrange
        String productionId = "prod-123";
        String newStatus = "STARTED";
        testProduction.setStatus(newStatus);
        
        when(productionProducerService.updateStatusAndPublish(productionId, newStatus))
            .thenReturn(testProduction);

        ProductionController.StatusUpdateRequest request = 
            new ProductionController.StatusUpdateRequest(newStatus);

        // Act
        ResponseEntity<?> response = controller.updateProductionStatus(productionId, request);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testProduction, response.getBody());
        verify(productionProducerService, times(1)).updateStatusAndPublish(productionId, newStatus);
    }

    @Test
    void testUpdateProductionStatusNotFound() {
        // Arrange
        String productionId = "prod-123";
        String newStatus = "STARTED";
        
        when(productionProducerService.updateStatusAndPublish(productionId, newStatus))
            .thenThrow(new ProductionException("Production not found"));

        ProductionController.StatusUpdateRequest request = 
            new ProductionController.StatusUpdateRequest(newStatus);

        // Act
        ResponseEntity<?> response = controller.updateProductionStatus(productionId, request);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertInstanceOf(ErrorResponse.class, response.getBody());
        ErrorResponse errorResponse = (ErrorResponse) response.getBody();
        assertEquals(404, errorResponse.getStatus());
        assertTrue(errorResponse.getMessage().contains("Production not found"));
    }

    @Test
    void testUpdateProductionStatusGenericException() {
        // Arrange
        String productionId = "prod-123";
        String newStatus = "STARTED";
        
        when(productionProducerService.updateStatusAndPublish(productionId, newStatus))
            .thenThrow(new RuntimeException("Database error"));

        ProductionController.StatusUpdateRequest request = 
            new ProductionController.StatusUpdateRequest(newStatus);

        // Act
        ResponseEntity<?> response = controller.updateProductionStatus(productionId, request);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertInstanceOf(ErrorResponse.class, response.getBody());
        ErrorResponse errorResponse = (ErrorResponse) response.getBody();
        assertEquals(500, errorResponse.getStatus());
        assertTrue(errorResponse.getMessage().contains("Database error"));
    }

    @Test
    void testStatusUpdateRequest() {
        // Test default constructor
        ProductionController.StatusUpdateRequest request1 = new ProductionController.StatusUpdateRequest();
        assertNull(request1.getStatus());

        // Test constructor with status
        ProductionController.StatusUpdateRequest request2 = 
            new ProductionController.StatusUpdateRequest("COMPLETED");
        assertEquals("COMPLETED", request2.getStatus());

        // Test setStatus
        request1.setStatus("STARTED");
        assertEquals("STARTED", request1.getStatus());
    }

    @Test
    void testUpdateProductionStatusMultipleTransitions() {
        // Arrange
        String productionId = "prod-123";
        String[] statuses = {"STARTED", "IN_PROGRESS", "COMPLETED"};
        
        for (String status : statuses) {
            testProduction.setStatus(status);
            when(productionProducerService.updateStatusAndPublish(productionId, status))
                .thenReturn(testProduction);

            // Act
            ProductionController.StatusUpdateRequest request = 
                new ProductionController.StatusUpdateRequest(status);
            ResponseEntity<?> response = controller.updateProductionStatus(productionId, request);

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(testProduction, response.getBody());
        }
    }

    @Test
    void testUpdateProductionStatusWithEmptyStatus() {
        // Arrange
        String productionId = "prod-123";
        String newStatus = "";
        
        when(productionProducerService.updateStatusAndPublish(productionId, newStatus))
            .thenReturn(testProduction);

        ProductionController.StatusUpdateRequest request = 
            new ProductionController.StatusUpdateRequest(newStatus);

        // Act
        ResponseEntity<?> response = controller.updateProductionStatus(productionId, request);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
