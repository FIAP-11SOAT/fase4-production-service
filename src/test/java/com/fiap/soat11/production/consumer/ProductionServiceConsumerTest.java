package com.fiap.soat11.production.consumer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fiap.soat11.production.dto.ConsumeDTO;
import com.fiap.soat11.production.dto.CustomerDTO;
import com.fiap.soat11.production.dto.ItensDTO;
import com.fiap.soat11.production.dto.PayloadDTO;
import com.fiap.soat11.production.service.ProductionConsumerService;

import java.util.Arrays;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
class ProductionServiceConsumerTest {

    @Mock
    private ProductionConsumerService productionConsumerService;

    private ProductionServiceConsumer productionServiceConsumer;

    @BeforeEach
    void setUp() {
        productionServiceConsumer = new ProductionServiceConsumer(productionConsumerService);
    }

    @Test
    void testListenCallsConsumerService() {
        // Arrange
        ConsumeDTO message = createValidConsumeDTO();

        // Act
        productionServiceConsumer.listen(message);

        // Assert
        verify(productionConsumerService, times(1)).handle(message);
    }

    @Test
    void testListenWithMultipleMessages() {
        // Arrange
        ConsumeDTO message1 = createValidConsumeDTO();
        ConsumeDTO message2 = createValidConsumeDTO();

        // Act
        productionServiceConsumer.listen(message1);
        productionServiceConsumer.listen(message2);

        // Assert
        verify(productionConsumerService, times(2)).handle(any(ConsumeDTO.class));
    }

    @Test
    void testListenWithDifferentPayloads() {
        // Arrange
        ConsumeDTO message = new ConsumeDTO();
        PayloadDTO payload = new PayloadDTO();
        payload.setId(UUID.randomUUID());
        
        CustomerDTO customer = new CustomerDTO();
        customer.setName("Jane Smith");
        payload.setCustomer(customer);
        
        ItensDTO item = new ItensDTO();
        item.setName("Product ABC");
        payload.setItens(Arrays.asList(item));
        
        message.setPayload(payload);

        // Act
        productionServiceConsumer.listen(message);

        // Assert
        verify(productionConsumerService, times(1)).handle(message);
    }

    @Test
    void testListenProcessesMessageCorrectly() {
        // Arrange
        ConsumeDTO message = createValidConsumeDTO();

        // Act
        productionServiceConsumer.listen(message);

        // Assert
        verify(productionConsumerService).handle(any(ConsumeDTO.class));
    }

    @Test
    void testListenWithMultipleItems() {
        // Arrange
        ConsumeDTO message = new ConsumeDTO();
        PayloadDTO payload = new PayloadDTO();
        payload.setId(UUID.randomUUID());
        
        CustomerDTO customer = new CustomerDTO();
        customer.setName("Customer A");
        payload.setCustomer(customer);
        
        ItensDTO item1 = new ItensDTO();
        item1.setName("Item 1");
        item1.setQuantity(2);
        
        ItensDTO item2 = new ItensDTO();
        item2.setName("Item 2");
        item2.setQuantity(3);
        
        payload.setItens(Arrays.asList(item1, item2));
        message.setPayload(payload);

        // Act
        productionServiceConsumer.listen(message);

        // Assert
        verify(productionConsumerService, times(1)).handle(message);
    }

    private ConsumeDTO createValidConsumeDTO() {
        ConsumeDTO consumeDTO = new ConsumeDTO();
        
        PayloadDTO payload = new PayloadDTO();
        payload.setId(UUID.randomUUID());
        
        CustomerDTO customer = new CustomerDTO();
        customer.setName("Test Customer");
        payload.setCustomer(customer);
        
        ItensDTO item = new ItensDTO();
        item.setName("Test Item");
        payload.setItens(Arrays.asList(item));
        
        consumeDTO.setPayload(payload);
        return consumeDTO;
    }
}
