package com.example.production.config.properties;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@ConfigurationProperties(prefix = "aws.sqs")
public class SqsProperties {
    
    @NotBlank(message = "Região AWS é obrigatória")
    private String region = "us-east-1";
    
    @NotBlank(message = "URL da fila de pedidos é obrigatória")
    private String orderQueueUrl;
    
    @NotBlank(message = "URL da fila de produção completada é obrigatória")
    private String productionCompletedQueueUrl;
    
    private int maxNumberOfMessages = 10;
    private int waitTimeSeconds = 20;
    private int visibilityTimeoutSeconds = 30;
}