package com.example.production.config.properties;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@ConfigurationProperties(prefix = "aws.cognito")
public class CognitoProperties {
    
    @NotBlank(message = "URI do JWK Set é obrigatória")
    private String jwkSetUri;
    
    private String userPoolId;
    private String clientId;
    private String region = "us-east-1";
}