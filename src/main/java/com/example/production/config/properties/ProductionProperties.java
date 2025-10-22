package com.example.production.config.properties;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@ConfigurationProperties(prefix = "production")
public class ProductionProperties {
    
    private Processing processing = new Processing();
    private Messaging messaging = new Messaging();
    
    @Data
    public static class Processing {
        private int maxRetries = 3;
        private long retryDelayMs = 5000;
        private boolean enableAsync = true;
        private int threadPoolSize = 10;
    }
    
    @Data
    public static class Messaging {
        private boolean enableDeadLetterQueue = true;
        private int maxReceiveCount = 3;
        private long messageRetentionPeriodSeconds = 1209600; // 14 dias
    }
}