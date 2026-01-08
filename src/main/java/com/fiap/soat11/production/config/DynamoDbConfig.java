package com.fiap.soat11.production.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import io.awspring.cloud.dynamodb.DynamoDbTableNameResolver;

@Configuration
public class DynamoDbConfig {
    @Bean
    public DynamoDbTableNameResolver dynamoDbTableNameResolver() {
        return new DynamoDbTableNameResolver() {
            @Override
            public <T> String resolve(Class<T> clazz) {
                // Procura a anotação @TableName na classe
                TableName tableNameAnno = clazz.getAnnotation(TableName.class);
                if (tableNameAnno != null && StringUtils.hasText(tableNameAnno.value())) {
                    return tableNameAnno.value();
                }

                // Fallback para o padrão (snake_case do nome da classe)
                String simpleName = clazz.getSimpleName();
                return simpleName.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
            }
        };
    }
}
