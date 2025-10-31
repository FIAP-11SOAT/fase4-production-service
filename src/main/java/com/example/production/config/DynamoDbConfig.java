package com.example.production.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.net.URI;

@Configuration
@Slf4j
public class DynamoDbConfig {

    @Value("${AWS_DEFAULT_REGION:us-east-1}")
    private String awsRegion;

    @Value("${aws.dynamodb.endpoint:}")
    private String dynamoDbEndpoint;

    @Value("${AWS_DYNAMODB_TABLE_NAME}")
    private String tableName;

    @Bean
    @Primary
    public DynamoDbClient dynamoDbClient() {
        log.info("Configuring DynamoDB client for region: {}", awsRegion);
        
        var builder = DynamoDbClient.builder()
                .region(Region.of(awsRegion))
                .credentialsProvider(DefaultCredentialsProvider.create());

        // Para desenvolvimento local com LocalStack
        if (dynamoDbEndpoint != null && !dynamoDbEndpoint.isEmpty()) {
            log.info("Using custom DynamoDB endpoint: {}", dynamoDbEndpoint);
            builder.endpointOverride(URI.create(dynamoDbEndpoint));
        }

        return builder.build();
    }

    @Bean
    public DynamoDbEnhancedClient dynamoDbEnhancedClient(DynamoDbClient dynamoDbClient) {
        return DynamoDbEnhancedClient.builder()
                .dynamoDbClient(dynamoDbClient)
                .build();
    }

    @Bean
    public String dynamoDbTableName() {
        return tableName;
    }
}