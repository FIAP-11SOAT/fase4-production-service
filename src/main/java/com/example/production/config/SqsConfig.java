package com.example.production.config;

import com.example.production.config.properties.SqsProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;

@Configuration
@EnableConfigurationProperties(SqsProperties.class)
@RequiredArgsConstructor
@Slf4j
public class SqsConfig {

    private final SqsProperties sqsProperties;

    @Bean
    public SqsClient sqsClient() {
        Region region = Region.of(sqsProperties.getAwsSqsRegion());
        log.info("Creating SQS client for region: {}", region);
        
        return SqsClient.builder()
                .region(region)
                .build();
    }
}
