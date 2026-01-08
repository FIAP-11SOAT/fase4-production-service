package com.fiap.soat11.production.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiap.soat11.production.config.ProductionConstants;
import com.fiap.soat11.production.entity.Customer;
import com.fiap.soat11.production.entity.OrderItem;
import com.fiap.soat11.production.entity.Production;
import com.fiap.soat11.production.exception.ProductionException;

import io.awspring.cloud.sqs.operations.SqsTemplate;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;

@ExtendWith(MockitoExtension.class)
class ProductionProducerServiceTest {

    @Mock
    private DynamoDbTable<Production> dynamoDBClient;

    @Mock
    private SqsTemplate sqsTemplate;

    @Mock
    private ObjectMapper objectMapper;

    private ProductionProducerService productionProducerService;
    private Production testProduction;
    private String productionId;

    @BeforeEach
    void setUp() {
        productionProducerService = new ProductionProducerService(dynamoDBClient, sqsTemplate, objectMapper);
        
        productionId = UUID.randomUUID().toString();
        
        Customer customer = new Customer();
        customer.setName("John Doe");

        OrderItem item = new OrderItem();
        item.setName("Test Product");
        item.setQuantity(2);

        testProduction = new Production();
        testProduction.setId(productionId);
        testProduction.setOrderID("order-123");
        testProduction.setStatus(ProductionConstants.PRODUCTION_STATUS_RECEIVED);
        testProduction.setCustomer(customer);
        testProduction.setItems(java.util.Arrays.asList(item));
        testProduction.setUpdatedAt(System.currentTimeMillis());
    }

    @Test
    void testUpdateStatusAndPublishSuccess() {
        // Arrange
        when(dynamoDBClient.getItem(any(Key.class))).thenReturn(testProduction);

        // Act
        Production result = productionProducerService.updateStatusAndPublish(productionId, "STARTED");

        // Assert
        assertNotNull(result);
        assertEquals("STARTED", result.getStatus());
        verify(dynamoDBClient, times(1)).putItem(any(Production.class));
    }

    @Test
    void testUpdateStatusAndPublishNotFound() {
        // Arrange
        when(dynamoDBClient.getItem(any(Key.class))).thenReturn(null);

        // Act & Assert
        assertThrows(ProductionException.class, () -> 
            productionProducerService.updateStatusAndPublish(productionId, "STARTED"));
        
        verify(dynamoDBClient, never()).putItem(any(Production.class));
    }

    @Test
    void testUpdateStatusAndPublishWithValidStatuses() {
        // Arrange
        when(dynamoDBClient.getItem(any(Key.class))).thenReturn(testProduction);
        String[] statuses = {"STARTED", "COMPLETED"};

        // Act & Assert
        for (String status : statuses) {
            Production result = productionProducerService.updateStatusAndPublish(productionId, status);
            assertEquals(status, result.getStatus());
        }

        verify(dynamoDBClient, times(2)).putItem(any(Production.class));
    }

    @Test
    void testUpdateStatusAndPublishUpdatesTimestamp() {
        // Arrange
        Long originalTimestamp = testProduction.getUpdatedAt();
        when(dynamoDBClient.getItem(any(Key.class))).thenReturn(testProduction);

        // Act
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        Production result = productionProducerService.updateStatusAndPublish(productionId, "STARTED");

        // Assert
        assertNotNull(result.getUpdatedAt());
        assertTrue(result.getUpdatedAt() >= originalTimestamp);
    }

    @Test
    void testUpdateStatusAndPublishThrowsExceptionOnDatabaseError() {
        // Arrange
        when(dynamoDBClient.getItem(any(Key.class)))
            .thenThrow(new RuntimeException("Database connection error"));

        // Act & Assert
        assertThrows(ProductionException.class, () -> 
            productionProducerService.updateStatusAndPublish(productionId, "STARTED"));
    }

    @Test
    void testUpdateStatusAndPublishMultipleItems() {
        // Arrange
        OrderItem item1 = new OrderItem();
        item1.setName("Product 1");
        item1.setQuantity(2);

        OrderItem item2 = new OrderItem();
        item2.setName("Product 2");
        item2.setQuantity(3);

        testProduction.setItems(java.util.Arrays.asList(item1, item2));
        when(dynamoDBClient.getItem(any(Key.class))).thenReturn(testProduction);

        // Act
        Production result = productionProducerService.updateStatusAndPublish(productionId, "STARTED");

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getItems().size());
        verify(dynamoDBClient, times(1)).putItem(any(Production.class));
    }

    @Test
    void testUpdateStatusAndPublishWithInvalidStatus() {
        // Arrange
        when(dynamoDBClient.getItem(any(Key.class))).thenReturn(testProduction);

        // Act & Assert
        assertThrows(ProductionException.class, () -> 
            productionProducerService.updateStatusAndPublish(productionId, "INVALID_STATUS"));
    }

    @Test
    void testUpdateStatusAndPublishPreservesOrderID() {
        // Arrange
        when(dynamoDBClient.getItem(any(Key.class))).thenReturn(testProduction);

        // Act
        Production result = productionProducerService.updateStatusAndPublish(productionId, "STARTED");

        // Assert
        assertEquals("order-123", result.getOrderID());
    }

    @Test
    void testUpdateStatusAndPublishPreservesCustomer() {
        // Arrange
        when(dynamoDBClient.getItem(any(Key.class))).thenReturn(testProduction);

        // Act
        Production result = productionProducerService.updateStatusAndPublish(productionId, "COMPLETED");

        // Assert
        assertNotNull(result.getCustomer());
        assertEquals("John Doe", result.getCustomer().getName());
    }

    @Test
    void testListPendingProductionsSuccess() {
        // Arrange
        Production prod1 = new Production();
        prod1.setId("prod-1");
        prod1.setStatus("RECEIVED");
        prod1.setUpdatedAt(1000L);

        Production prod2 = new Production();
        prod2.setId("prod-2");
        prod2.setStatus("IN_PROGRESS");
        prod2.setUpdatedAt(2000L);

        Production prod3 = new Production();
        prod3.setId("prod-3");
        prod3.setStatus("STARTED");
        prod3.setUpdatedAt(1500L);

        Production prod4 = new Production();
        prod4.setId("prod-4");
        prod4.setStatus("COMPLETED");
        prod4.setUpdatedAt(3000L);

        java.util.List<Production> allProductions = java.util.Arrays.asList(prod1, prod2, prod3, prod4);
        
        software.amazon.awssdk.enhanced.dynamodb.model.PageIterable<Production> mockPageIterable = 
            mock(software.amazon.awssdk.enhanced.dynamodb.model.PageIterable.class);
        software.amazon.awssdk.core.pagination.sync.SdkIterable<Production> mockSdkIterable =
            mock(software.amazon.awssdk.core.pagination.sync.SdkIterable.class);
        when(dynamoDBClient.scan()).thenReturn(mockPageIterable);
        when(mockPageIterable.items()).thenReturn(mockSdkIterable);
        when(mockSdkIterable.stream()).thenReturn(allProductions.stream());

        // Act
        java.util.List<Production> result = productionProducerService.listPendingProductions();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("prod-1", result.get(0).getId());
        assertEquals("RECEIVED", result.get(0).getStatus());
        assertEquals("prod-2", result.get(1).getId());
        assertEquals("IN_PROGRESS", result.get(1).getStatus());
        verify(dynamoDBClient, times(1)).scan();
    }

    @Test
    void testListPendingProductionsOrderedByUpdatedAt() {
        // Arrange
        Production prod1 = new Production();
        prod1.setId("prod-1");
        prod1.setStatus("RECEIVED");
        prod1.setUpdatedAt(3000L);

        Production prod2 = new Production();
        prod2.setId("prod-2");
        prod2.setStatus("IN_PROGRESS");
        prod2.setUpdatedAt(1000L);

        Production prod3 = new Production();
        prod3.setId("prod-3");
        prod3.setStatus("PENDING");
        prod3.setUpdatedAt(2000L);

        java.util.List<Production> allProductions = java.util.Arrays.asList(prod1, prod2, prod3);
        
        software.amazon.awssdk.enhanced.dynamodb.model.PageIterable<Production> mockPageIterable = 
            mock(software.amazon.awssdk.enhanced.dynamodb.model.PageIterable.class);
        software.amazon.awssdk.core.pagination.sync.SdkIterable<Production> mockSdkIterable =
            mock(software.amazon.awssdk.core.pagination.sync.SdkIterable.class);
        when(dynamoDBClient.scan()).thenReturn(mockPageIterable);
        when(mockPageIterable.items()).thenReturn(mockSdkIterable);
        when(mockSdkIterable.stream()).thenReturn(allProductions.stream());

        // Act
        java.util.List<Production> result = productionProducerService.listPendingProductions();

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals(1000L, result.get(0).getUpdatedAt());
        assertEquals(2000L, result.get(1).getUpdatedAt());
        assertEquals(3000L, result.get(2).getUpdatedAt());
    }

    @Test
    void testListPendingProductionsEmptyTable() {
        // Arrange
        software.amazon.awssdk.enhanced.dynamodb.model.PageIterable<Production> mockPageIterable = 
            mock(software.amazon.awssdk.enhanced.dynamodb.model.PageIterable.class);
        software.amazon.awssdk.core.pagination.sync.SdkIterable<Production> mockSdkIterable =
            mock(software.amazon.awssdk.core.pagination.sync.SdkIterable.class);
        when(dynamoDBClient.scan()).thenReturn(mockPageIterable);
        when(mockPageIterable.items()).thenReturn(mockSdkIterable);
        when(mockSdkIterable.stream()).thenReturn(java.util.stream.Stream.empty());

        // Act
        java.util.List<Production> result = productionProducerService.listPendingProductions();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(dynamoDBClient, times(1)).scan();
    }

    @Test
    void testListPendingProductionsAllStartedOrCompleted() {
        // Arrange
        Production prod1 = new Production();
        prod1.setId("prod-1");
        prod1.setStatus("STARTED");
        prod1.setUpdatedAt(1000L);

        Production prod2 = new Production();
        prod2.setId("prod-2");
        prod2.setStatus("COMPLETED");
        prod2.setUpdatedAt(2000L);

        java.util.List<Production> allProductions = java.util.Arrays.asList(prod1, prod2);
        
        software.amazon.awssdk.enhanced.dynamodb.model.PageIterable<Production> mockPageIterable = 
            mock(software.amazon.awssdk.enhanced.dynamodb.model.PageIterable.class);
        software.amazon.awssdk.core.pagination.sync.SdkIterable<Production> mockSdkIterable =
            mock(software.amazon.awssdk.core.pagination.sync.SdkIterable.class);
        when(dynamoDBClient.scan()).thenReturn(mockPageIterable);
        when(mockPageIterable.items()).thenReturn(mockSdkIterable);
        when(mockSdkIterable.stream()).thenReturn(allProductions.stream());

        // Act
        java.util.List<Production> result = productionProducerService.listPendingProductions();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testListPendingProductionsHandlesNullStatus() {
        // Arrange
        Production prod1 = new Production();
        prod1.setId("prod-1");
        prod1.setStatus(null);
        prod1.setUpdatedAt(1000L);

        Production prod2 = new Production();
        prod2.setId("prod-2");
        prod2.setStatus("RECEIVED");
        prod2.setUpdatedAt(2000L);

        java.util.List<Production> allProductions = java.util.Arrays.asList(prod1, prod2);
        
        software.amazon.awssdk.enhanced.dynamodb.model.PageIterable<Production> mockPageIterable = 
            mock(software.amazon.awssdk.enhanced.dynamodb.model.PageIterable.class);
        software.amazon.awssdk.core.pagination.sync.SdkIterable<Production> mockSdkIterable =
            mock(software.amazon.awssdk.core.pagination.sync.SdkIterable.class);
        when(dynamoDBClient.scan()).thenReturn(mockPageIterable);
        when(mockPageIterable.items()).thenReturn(mockSdkIterable);
        when(mockSdkIterable.stream()).thenReturn(allProductions.stream());

        // Act
        java.util.List<Production> result = productionProducerService.listPendingProductions();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("prod-2", result.get(0).getId());
    }

    @Test
    void testListPendingProductionsHandlesNullUpdatedAt() {
        // Arrange
        Production prod1 = new Production();
        prod1.setId("prod-1");
        prod1.setStatus("RECEIVED");
        prod1.setUpdatedAt(null);

        Production prod2 = new Production();
        prod2.setId("prod-2");
        prod2.setStatus("IN_PROGRESS");
        prod2.setUpdatedAt(1000L);

        java.util.List<Production> allProductions = java.util.Arrays.asList(prod1, prod2);
        
        software.amazon.awssdk.enhanced.dynamodb.model.PageIterable<Production> mockPageIterable = 
            mock(software.amazon.awssdk.enhanced.dynamodb.model.PageIterable.class);
        software.amazon.awssdk.core.pagination.sync.SdkIterable<Production> mockSdkIterable =
            mock(software.amazon.awssdk.core.pagination.sync.SdkIterable.class);
        when(dynamoDBClient.scan()).thenReturn(mockPageIterable);
        when(mockPageIterable.items()).thenReturn(mockSdkIterable);
        when(mockSdkIterable.stream()).thenReturn(allProductions.stream());

        // Act
        java.util.List<Production> result = productionProducerService.listPendingProductions();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("prod-1", result.get(0).getId());
        assertEquals("prod-2", result.get(1).getId());
    }

    @Test
    void testListPendingProductionsCaseInsensitiveStatus() {
        // Arrange
        Production prod1 = new Production();
        prod1.setId("prod-1");
        prod1.setStatus("started");
        prod1.setUpdatedAt(1000L);

        Production prod2 = new Production();
        prod2.setId("prod-2");
        prod2.setStatus("Completed");
        prod2.setUpdatedAt(2000L);

        Production prod3 = new Production();
        prod3.setId("prod-3");
        prod3.setStatus("RECEIVED");
        prod3.setUpdatedAt(3000L);

        java.util.List<Production> allProductions = java.util.Arrays.asList(prod1, prod2, prod3);
        
        software.amazon.awssdk.enhanced.dynamodb.model.PageIterable<Production> mockPageIterable = 
            mock(software.amazon.awssdk.enhanced.dynamodb.model.PageIterable.class);
        software.amazon.awssdk.core.pagination.sync.SdkIterable<Production> mockSdkIterable =
            mock(software.amazon.awssdk.core.pagination.sync.SdkIterable.class);
        when(dynamoDBClient.scan()).thenReturn(mockPageIterable);
        when(mockPageIterable.items()).thenReturn(mockSdkIterable);
        when(mockSdkIterable.stream()).thenReturn(allProductions.stream());

        // Act
        java.util.List<Production> result = productionProducerService.listPendingProductions();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("prod-3", result.get(0).getId());
    }

    @Test
    void testListPendingProductionsThrowsExceptionOnDatabaseError() {
        // Arrange
        when(dynamoDBClient.scan())
            .thenThrow(new RuntimeException("Database connection error"));

        // Act & Assert
        assertThrows(ProductionException.class, () -> 
            productionProducerService.listPendingProductions());
    }
}
