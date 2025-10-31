package com.example.production.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.GetParameterRequest;
import software.amazon.awssdk.services.ssm.model.GetParameterResponse;
import software.amazon.awssdk.services.ssm.model.ParameterNotFoundException;

import java.net.URI;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class ParameterStoreConfig {

    @Value("${AWS_DEFAULT_REGION:us-east-1}")
    private String awsRegion;

    @Value("${aws.ssm.endpoint:}")
    private String ssmEndpoint;

    @Value("${spring.application.name:production-service}")
    private String applicationName;

    @Value("${spring.profiles.active:production}")
    private String environment;

    @Bean
    public SsmClient ssmClient() {
        log.info("Configuring SSM client for region: {}", awsRegion);
        
        var builder = SsmClient.builder()
                .region(Region.of(awsRegion))
                .credentialsProvider(DefaultCredentialsProvider.create());

        // Para desenvolvimento local com LocalStack
        if (ssmEndpoint != null && !ssmEndpoint.isEmpty()) {
            log.info("Using custom SSM endpoint: {}", ssmEndpoint);
            builder.endpointOverride(URI.create(ssmEndpoint));
        }

        return builder.build();
    }

    /**
     * Utility method to get parameter from Parameter Store
     */
    public String getParameter(String parameterName) {
        return getParameter(parameterName, false);
    }

    /**
     * Utility method to get parameter from Parameter Store
     */
    public String getParameter(String parameterName, boolean decrypt) {
        try {
            SsmClient ssmClient = ssmClient();
            GetParameterRequest request = GetParameterRequest.builder()
                    .name(parameterName)
                    .withDecryption(decrypt)
                    .build();

            GetParameterResponse response = ssmClient.getParameter(request);
            String value = response.parameter().value();
            
            log.debug("Retrieved parameter: {} = {}", parameterName, 
                     decrypt ? "***ENCRYPTED***" : value);
            
            return value;
        } catch (ParameterNotFoundException e) {
            log.warn("Parameter not found: {}", parameterName);
            return null;
        } catch (Exception e) {
            log.error("Error retrieving parameter: {}", parameterName, e);
            return null;
        }
    }

    /**
     * Get parameter with application and environment prefix
     */
    public String getApplicationParameter(String parameterKey) {
        String fullParameterName = String.format("/%s/%s/%s", 
                                                applicationName, environment, parameterKey);
        return getParameter(fullParameterName);
    }

    /**
     * Get encrypted parameter with application and environment prefix
     */
    public String getApplicationSecret(String parameterKey) {
        String fullParameterName = String.format("/%s/%s/secrets/%s", 
                                                applicationName, environment, parameterKey);
        return getParameter(fullParameterName, true);
    }
}