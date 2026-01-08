package com.fiap.soat11.production.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fiap.soat11.production.entity.Production;

import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

@Configuration
public class DynamoDbTableConfig {

    @Bean
    public DynamoDbTable<Production> productionTable(DynamoDbEnhancedClient client) {
        return client.table(
            ProductionConstants.DYNAMODB_TABLE_NAME,
            TableSchema.fromBean(Production.class)
        );
    }
}
