package com.example.production;

import com.example.production.config.properties.ProductionProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(ProductionProperties.class)
public class ProductionServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ProductionServiceApplication.class, args);
    }
}
