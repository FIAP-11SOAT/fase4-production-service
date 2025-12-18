package com.example.production.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for AWS Parameter Store using Spring Cloud AWS.
 * SSM client is auto-configured by spring-cloud-aws-starter-parameter-store.
 * 
 * Spring Cloud AWS automatically integrates Parameter Store with Spring's
 * Environment abstraction. You can access parameters using @Value or Environment.
 * 
 * Example usage in other beans:
 * @Value("${/production-service/production/database-url}") 
 * private String databaseUrl;
 * 
 * Configuration properties:
 * - spring.cloud.aws.parameterstore.enabled=true
 * - spring.cloud.aws.parameterstore.prefix=/production-service/production
 * - spring.cloud.aws.parameterstore.profile-separator=_
 */
@Configuration
@Slf4j
public class ParameterStoreConfig {
    
    public ParameterStoreConfig() {
        log.info("Parameter Store integration enabled via Spring Cloud AWS");
    }
}