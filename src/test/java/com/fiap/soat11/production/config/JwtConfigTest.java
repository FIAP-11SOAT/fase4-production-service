package com.fiap.soat11.production.config;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.util.ReflectionTestUtils;

class JwtConfigTest {

    private JwtConfig jwtConfig;
    private String validJwkJson;

    @BeforeEach
    void setUp() {
        jwtConfig = new JwtConfig();
        
        // Use a sample JWK JSON for testing
        validJwkJson = "{\"kty\":\"RSA\",\"use\":\"sig\",\"kid\":\"test-key\",\"n\":\"0vx7agoebGcQSuuPiLJXZptN9nndrQmbXEps2aiAFbWhM78LhWx4cbbfAAtVT86zwu1RK7aPFFxuhDR1L6tSoc_BJECPebWKRXjBZCiFV4n3oknjhMstn64tZ_2W-5JsGY4Hc5n9yBXArwl93lqt7_RN5w6Cf0h4QyQ5v-65YGjQR0_FDW2QvzqY368QQMicAtaSqzs8KJZgnYb9c7d0zgdAZHzu6qMQvRL5hajrn1n91CbOpbISD08qNLyrdkt-bFTWhAI4vMQFh6WeZu0fM4lFd2NcRwr3XPksINHaQ-G_xBniIqbw0Ls1jF44-csFCur-kEgU8awapJzKnqDKgw\",\"e\":\"AQAB\"}";
    }

    @Test
    void testJwtConfigInitialization() {
        // Test that JwtConfig can be instantiated
        assertNotNull(jwtConfig);
    }

    @Test
    void testJwtDecoderBeanWithValidJwk() {
        // Arrange
        ReflectionTestUtils.setField(jwtConfig, "jwkJson", validJwkJson);

        // Act
        JwtDecoder jwtDecoder = jwtConfig.jwtDecoder();

        // Assert
        assertNotNull(jwtDecoder);
    }

    @Test
    void testJwtDecoderBeanWithInvalidJwk() {
        // Arrange
        String invalidJwkJson = "{invalid json}";
        ReflectionTestUtils.setField(jwtConfig, "jwkJson", invalidJwkJson);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            jwtConfig.jwtDecoder();
        });
    }

    @Test
    void testJwtDecoderBeanThrowsExceptionOnParseError() {
        // Arrange
        ReflectionTestUtils.setField(jwtConfig, "jwkJson", "{\"kty\":\"RSA\"}"); // Incomplete JWK

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            jwtConfig.jwtDecoder();
        });
        assertTrue(exception.getMessage().contains("Falha ao carregar ou parsear"));
    }

    @Test
    void testJwtConfigWithEmptyJwk() {
        // Arrange
        ReflectionTestUtils.setField(jwtConfig, "jwkJson", "");

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            jwtConfig.jwtDecoder();
        });
    }

    @Test
    void testJwtConfigWithNullJwk() {
        // Arrange
        ReflectionTestUtils.setField(jwtConfig, "jwkJson", null);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            jwtConfig.jwtDecoder();
        });
    }
}
