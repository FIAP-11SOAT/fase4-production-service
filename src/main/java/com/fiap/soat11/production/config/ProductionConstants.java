package com.fiap.soat11.production.config;

public class ProductionConstants {
    
    public static final String DYNAMODB_TABLE_NAME = "fase4-production-service-table";
    public static final String SQS_QUEUE_NAME = "fase4-production-service-queue";
    public static final String PRODUCTION_STATUS_RECEIVED = "RECEIVED";
    
    private ProductionConstants() {
        // Utility class
    }
}
