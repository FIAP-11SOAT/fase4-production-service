package com.fiap.soat11.production.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.fiap.soat11.production.config.ProductionConstants;
import com.fiap.soat11.production.dto.ConsumeDTO;
import com.fiap.soat11.production.service.ProductionConsumerService;

import io.awspring.cloud.sqs.annotation.SqsListener;

@Service
public class ProductionServiceConsumer {
    
    private static final Logger logger = LoggerFactory.getLogger(ProductionServiceConsumer.class);

    private final ProductionConsumerService productionConsumerService;

    public ProductionServiceConsumer(ProductionConsumerService productionConsumerService) {
        this.productionConsumerService = productionConsumerService;
    }

    @SqsListener(ProductionConstants.SQS_QUEUE_NAME)
    public void listen(ConsumeDTO message) {
        logger.debug("Received message from SQS queue");
        productionConsumerService.handle(message);
    }

}
