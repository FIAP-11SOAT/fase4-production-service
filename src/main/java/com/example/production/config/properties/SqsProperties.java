package com.example.production.config.properties;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@ConfigurationProperties
public class SqsProperties {
    
    @NotBlank(message = "Região AWS é obrigatória")
    private String awsSqsRegion = "us-east-1";
    
    @NotBlank(message = "URL da fila de pedidos é obrigatória")
    private String awsSqsOrderQueueUrl;
    
    @NotBlank(message = "URL da fila de produção completada é obrigatória")
    private String awsSqsProductionCompletedQueueUrl;
    
    private int awsSqsMaxNumberOfMessages = 10;
    private int awsSqsWaitTimeSeconds = 20;
    private int awsSqsVisibilityTimeoutSeconds = 30;
}