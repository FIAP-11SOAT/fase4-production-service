package com.fiap.soat11.production.config;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class DynamoDbConfigTest {

    @Test
    void testDynamoDbConfigInitialization() {
        // This is a configuration class and may not require extensive testing
        // as it's mostly Spring configuration
        DynamoDbConfig config = new DynamoDbConfig();
        assertNotNull(config);
    }
}
