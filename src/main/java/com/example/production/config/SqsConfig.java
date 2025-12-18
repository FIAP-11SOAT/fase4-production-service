package com.example.production.config;

import com.example.production.config.properties.SqsProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for SQS using Spring Cloud AWS.
 * SQS client is auto-configured by spring-cloud-aws-starter-sqs.
 * Queue URLs and settings are configured via properties.
 */
@Configuration
@EnableConfigurationProperties(SqsProperties.class)
@Slf4j
public class SqsConfig {
}
