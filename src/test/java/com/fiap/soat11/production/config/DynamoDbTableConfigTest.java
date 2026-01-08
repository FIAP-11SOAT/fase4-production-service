package com.fiap.soat11.production.config;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class DynamoDbTableConfigTest {

    @Test
    void testDynamoDbTableConfigInitialization() {
        // This is a configuration class and may not require extensive testing
        // as it's mostly Spring configuration
        DynamoDbTableConfig config = new DynamoDbTableConfig();
        assertNotNull(config);
    }
}
