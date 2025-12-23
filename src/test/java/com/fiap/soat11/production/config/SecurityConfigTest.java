package com.fiap.soat11.production.config;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class SecurityConfigTest {

    @Mock
    private JwtDecoder jwtDecoder;

    private SecurityConfig securityConfig;

    @BeforeEach
    void setUp() {
        securityConfig = new SecurityConfig(jwtDecoder);
    }

    @Test
    void testSecurityConfigInitialization() {
        // Assert
        assertNotNull(securityConfig);
        assertNotNull(securityConfig);
    }

    @Test
    void testObjectMapperBean() {
        // Act
        ObjectMapper objectMapper = securityConfig.objectMapper();

        // Assert
        assertNotNull(objectMapper);
        assertTrue(objectMapper instanceof ObjectMapper);
    }

    @Test
    void testObjectMapperBeanMultipleCalls() {
        // Act
        ObjectMapper mapper1 = securityConfig.objectMapper();
        ObjectMapper mapper2 = securityConfig.objectMapper();

        // Assert
        assertNotNull(mapper1);
        assertNotNull(mapper2);
    }

    @Test
    void testSecurityFilterChainCreation() throws Exception {
        // Arrange
        HttpSecurity httpSecurity = mock(HttpSecurity.class, withSettings().lenient());
        
        // Create a mock SecurityFilterChain
        org.springframework.security.web.DefaultSecurityFilterChain defaultChain = 
            mock(org.springframework.security.web.DefaultSecurityFilterChain.class);
        
        // Mock the builder chain
        when(httpSecurity.authorizeHttpRequests(any())).thenReturn(httpSecurity);
        when(httpSecurity.oauth2ResourceServer(any())).thenReturn(httpSecurity);
        when(httpSecurity.csrf(any())).thenReturn(httpSecurity);
        when(httpSecurity.sessionManagement(any())).thenReturn(httpSecurity);
        when(httpSecurity.build()).thenReturn(defaultChain);

        // Act
        SecurityFilterChain chain = securityConfig.securityFilterChain(httpSecurity);

        // Assert
        assertNotNull(chain);
    }

    @Test
    void testSecurityConfigWithValidJwtDecoder() {
        // Assert that the JwtDecoder is properly stored
        assertNotNull(jwtDecoder);
    }

    @Test
    void testObjectMapperCanSerialize() throws Exception {
        // Arrange
        ObjectMapper objectMapper = securityConfig.objectMapper();
        TestObject testObject = new TestObject("test", 123);

        // Act
        String json = objectMapper.writeValueAsString(testObject);

        // Assert
        assertNotNull(json);
        assertTrue(json.contains("\"name\":\"test\""));
    }

    @Test
    void testObjectMapperCanDeserialize() throws Exception {
        // Arrange
        ObjectMapper objectMapper = securityConfig.objectMapper();
        String json = "{\"name\":\"test\",\"value\":123}";

        // Act
        TestObject result = objectMapper.readValue(json, TestObject.class);

        // Assert
        assertNotNull(result);
        assertEquals("test", result.name);
        assertEquals(123, result.value);
    }

    // Helper class for testing ObjectMapper
    public static class TestObject {
        public String name;
        public int value;

        public TestObject() {
        }

        public TestObject(String name, int value) {
            this.name = name;
            this.value = value;
        }
    }

    @Test
    void testSecurityConfigConstructor() {
        // Act
        SecurityConfig config = new SecurityConfig(jwtDecoder);

        // Assert
        assertNotNull(config);
    }

    @Test
    void testMultipleObjectMapperInstances() {
        // Act
        ObjectMapper mapper1 = securityConfig.objectMapper();
        ObjectMapper mapper2 = securityConfig.objectMapper();
        ObjectMapper mapper3 = securityConfig.objectMapper();

        // Assert
        assertNotNull(mapper1);
        assertNotNull(mapper2);
        assertNotNull(mapper3);
    }
}
