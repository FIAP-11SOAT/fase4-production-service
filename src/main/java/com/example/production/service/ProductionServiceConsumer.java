package com.example.production.service;

import io.awspring.cloud.sqs.annotation.SqsListener;

public class ProductionServiceConsumer {
    
    @SqsListener("fase4-production-service-queue")
    public void listen(String message) {
        System.out.println(message);
    }
}
