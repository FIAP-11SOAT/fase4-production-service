package com.fiap.soat11.production.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
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
}
