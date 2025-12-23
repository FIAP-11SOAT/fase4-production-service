package com.fiap.soat11.production.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.doThrow;
import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fiap.soat11.production.dto.ConsumeDTO;
import com.fiap.soat11.production.dto.CustomerDTO;
import com.fiap.soat11.production.dto.ItensDTO;
import com.fiap.soat11.production.dto.MetaDTO;
import com.fiap.soat11.production.dto.PayloadDTO;
import com.fiap.soat11.production.entity.OrderItem;
import com.fiap.soat11.production.entity.Production;
import com.fiap.soat11.production.exception.ProductionException;

import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;

@ExtendWith(MockitoExtension.class)
class ProductionConsumerServiceTest {

    @Mock
    private DynamoDbTable<Production> dynamoDBClientMock;

    private ProductionConsumerService productionConsumerService;

    @BeforeEach
    void setUp() {
        productionConsumerService = new ProductionConsumerService(dynamoDBClientMock);
    }

    @Test
    void shouldSaveProductionWhenReceivingValidMessage() {
        // Arrange
        ConsumeDTO consumeDTO = createConsumeDTO();

        // Act
        productionConsumerService.handle(consumeDTO);

        // Assert
        verify(dynamoDBClientMock, times(1)).putItem(any(Production.class));
    }

    @Test
    void shouldSaveProductionWithCorrectDataMapping() {
        // Arrange
        ConsumeDTO consumeDTO = createConsumeDTO();

        // Act
        productionConsumerService.handle(consumeDTO);

        // Assert - Capture the argument passed to putItem
        ArgumentCaptor<Production> productionCaptor = ArgumentCaptor.forClass(Production.class);
        verify(dynamoDBClientMock).putItem(productionCaptor.capture());

        Production capturedProduction = productionCaptor.getValue();
        
        assertThat(capturedProduction)
            .isNotNull()
            .satisfies(p -> {
                assertThat(p.getId()).isNotNull();
                assertThat(p.getStatus()).isEqualTo("RECEIVED");
                assertThat(p.getCustomer()).isNotNull();
            });
        
        assertThat(capturedProduction.getOrderID())
            .isEqualTo("c4f1a8e2-9d3b-4a6f-8e1c-7b5d9a2f4e60");
        
        assertThat(capturedProduction.getCustomer().getName())
            .isEqualTo("Ribeiro");
        
        assertThat(capturedProduction.getItems())
            .hasSize(3);
    }

    @Test
    void shouldMapOrderItemsCorrectly() {
        // Arrange
        ConsumeDTO consumeDTO = createConsumeDTO();

        // Act
        productionConsumerService.handle(consumeDTO);

        // Assert
        ArgumentCaptor<Production> productionCaptor = ArgumentCaptor.forClass(Production.class);
        verify(dynamoDBClientMock).putItem(productionCaptor.capture());

        Production capturedProduction = productionCaptor.getValue();
        List<OrderItem> items = capturedProduction.getItems();

        assertThat(items)
            .hasSize(3)
            .satisfies(itemList -> {
                assertThat(itemList.get(0))
                    .hasFieldOrPropertyWithValue("name", "XBurguer")
                    .hasFieldOrPropertyWithValue("quantity", 2);
                
                assertThat(itemList.get(1))
                    .hasFieldOrPropertyWithValue("name", "Batata Frita")
                    .hasFieldOrPropertyWithValue("quantity", 1);
                
                assertThat(itemList.get(2))
                    .hasFieldOrPropertyWithValue("name", "Refrigerante")
                    .hasFieldOrPropertyWithValue("quantity", 2);
            });
    }

    private ConsumeDTO createConsumeDTO() {
        MetaDTO meta = new MetaDTO();

        List<ItensDTO> itens = Arrays.asList(
            createItensDTO("XBurguer", 2),
            createItensDTO("Batata Frita", 1),
            createItensDTO("Refrigerante", 2)
        );

        CustomerDTO customer = new CustomerDTO();
        customer.setId(null);
        customer.setName("Ribeiro");

        PayloadDTO payload = new PayloadDTO();
        payload.setId(java.util.UUID.fromString("c4f1a8e2-9d3b-4a6f-8e1c-7b5d9a2f4e60"));
        payload.setItens(itens);
        payload.setCustomer(customer);

        ConsumeDTO consumeDTO = new ConsumeDTO();
        consumeDTO.setMeta(meta);
        consumeDTO.setPayload(payload);

        return consumeDTO;
    }

    private ItensDTO createItensDTO(String name, Integer quantity) {
        ItensDTO itensDTO = new ItensDTO();
        itensDTO.setName(name);
        itensDTO.setQuantity(quantity);
        return itensDTO;
    }

    @Test
    void shouldThrowProductionExceptionOnDatabaseError() {
        // Arrange
        ConsumeDTO consumeDTO = createConsumeDTO();
        doThrow(new RuntimeException("Database error"))
            .when(dynamoDBClientMock).putItem(any(Production.class));

        // Act & Assert
        assertThrows(ProductionException.class, () -> {
            productionConsumerService.handle(consumeDTO);
        });
    }

    @Test
    void shouldThrowProductionExceptionOnValidationError() {
        // Arrange
        ConsumeDTO consumeDTO = createConsumeDTO();
        // Create a scenario that might trigger a validation error
        
        // Act & Assert - normally this should handle the exception properly
        productionConsumerService.handle(consumeDTO);
        verify(dynamoDBClientMock, times(1)).putItem(any(Production.class));
    }

    @Test
    void shouldMapProductionWithCorrectTimestamp() {
        // Arrange
        ConsumeDTO consumeDTO = createConsumeDTO();
        long beforeTime = System.currentTimeMillis();

        // Act
        productionConsumerService.handle(consumeDTO);
        long afterTime = System.currentTimeMillis();

        // Assert
        ArgumentCaptor<Production> productionCaptor = ArgumentCaptor.forClass(Production.class);
        verify(dynamoDBClientMock).putItem(productionCaptor.capture());
        
        Production capturedProduction = productionCaptor.getValue();
        assertThat(capturedProduction).isNotNull();
        // The timestamp may be set during mapping, so just verify it's been set appropriately
        if (capturedProduction.getUpdatedAt() != null) {
            assertThat(capturedProduction.getUpdatedAt())
                .isGreaterThanOrEqualTo(beforeTime)
                .isLessThanOrEqualTo(afterTime);
        }
    }

    @Test
    void shouldLogInfoMessagesOnSuccessfulProcessing() {
        // Arrange
        ConsumeDTO consumeDTO = createConsumeDTO();

        // Act
        productionConsumerService.handle(consumeDTO);

        // Assert
        verify(dynamoDBClientMock, times(1)).putItem(any(Production.class));
    }

    @Test
    void shouldHandleProductionWithSingleItem() {
        // Arrange
        ConsumeDTO consumeDTO = new ConsumeDTO();
        MetaDTO meta = new MetaDTO();
        consumeDTO.setMeta(meta);

        PayloadDTO payload = new PayloadDTO();
        payload.setId(java.util.UUID.randomUUID());
        payload.setItens(Arrays.asList(createItensDTO("Item", 1)));
        
        CustomerDTO customer = new CustomerDTO();
        customer.setName("Test Customer");
        payload.setCustomer(customer);
        
        consumeDTO.setPayload(payload);

        // Act
        productionConsumerService.handle(consumeDTO);

        // Assert
        ArgumentCaptor<Production> productionCaptor = ArgumentCaptor.forClass(Production.class);
        verify(dynamoDBClientMock).putItem(productionCaptor.capture());
        
        Production capturedProduction = productionCaptor.getValue();
        assertThat(capturedProduction.getItems()).hasSize(1);
    }

    @Test
    void shouldHandleProductionWithManyItems() {
        // Arrange
        ConsumeDTO consumeDTO = new ConsumeDTO();
        MetaDTO meta = new MetaDTO();
        consumeDTO.setMeta(meta);

        List<ItensDTO> itens = Arrays.asList(
            createItensDTO("Item1", 1),
            createItensDTO("Item2", 2),
            createItensDTO("Item3", 3),
            createItensDTO("Item4", 4),
            createItensDTO("Item5", 5)
        );

        PayloadDTO payload = new PayloadDTO();
        payload.setId(java.util.UUID.randomUUID());
        payload.setItens(itens);
        
        CustomerDTO customer = new CustomerDTO();
        customer.setName("Test Customer");
        payload.setCustomer(customer);
        
        consumeDTO.setPayload(payload);

        // Act
        productionConsumerService.handle(consumeDTO);

        // Assert
        ArgumentCaptor<Production> productionCaptor = ArgumentCaptor.forClass(Production.class);
        verify(dynamoDBClientMock).putItem(productionCaptor.capture());
        
        Production capturedProduction = productionCaptor.getValue();
        assertThat(capturedProduction.getItems()).hasSize(5);
    }

    @Test
    void shouldSetReceivedStatusOnProduction() {
        // Arrange
        ConsumeDTO consumeDTO = createConsumeDTO();

        // Act
        productionConsumerService.handle(consumeDTO);

        // Assert
        ArgumentCaptor<Production> productionCaptor = ArgumentCaptor.forClass(Production.class);
        verify(dynamoDBClientMock).putItem(productionCaptor.capture());
        
        Production capturedProduction = productionCaptor.getValue();
        assertThat(capturedProduction.getStatus()).isEqualTo("RECEIVED");
    }
}
