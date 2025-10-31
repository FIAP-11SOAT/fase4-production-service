package com.example.production.config.properties;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@ConfigurationProperties
public class CognitoProperties {
    
    @NotBlank(message = "URI do JWK Set é obrigatória")
    private String awsCognitoJwkSetUri;
    
    private String awsCognitoUserPoolId;
    private String awsCognitoClientId;
    private String awsCognitoRegion = "us-east-1";
}