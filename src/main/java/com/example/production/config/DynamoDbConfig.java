package com.example.production.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for DynamoDB using Spring Cloud AWS.
 * DynamoDB clients are auto-configured by spring-cloud-aws-starter-dynamodb.
 * Custom endpoint configuration (e.g., LocalStack) is done via properties.
 */
@Configuration
@Slf4j
public class DynamoDbConfig {

    @Value("${AWS_DYNAMODB_TABLE_NAME}")
    private String tableName;

    @Bean
    public String dynamoDbTableName() {
        log.info("Using DynamoDB table: {}", tableName);
        return tableName;
    }
}