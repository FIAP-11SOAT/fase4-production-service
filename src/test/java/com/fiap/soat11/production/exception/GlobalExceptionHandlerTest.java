package com.fiap.soat11.production.exception;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.server.ResponseStatusException;

import com.fiap.soat11.production.dto.ErrorResponse;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    void testHandleAuthorizationDeniedException() {
        // Arrange
        AuthorizationDeniedException exception = new AuthorizationDeniedException("Access denied");

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleAuthorizationDeniedException(exception);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(403, response.getBody().getStatus());
        assertTrue(response.getBody().getMessage().contains("AuthorizationDeniedException"));
    }

    @Test
    void testHandleAccessDeniedException() {
        // Arrange
        AccessDeniedException exception = new AccessDeniedException("Access denied");

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleAccessDeniedException(exception);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(403, response.getBody().getStatus());
        assertTrue(response.getBody().getMessage().contains("AccessDeniedException"));
    }

    @Test
    void testHandleAuthenticationException() {
        // Arrange
        AuthenticationException exception = new AuthenticationException("Authentication failed") {};

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleAuthenticationException(exception);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(401, response.getBody().getStatus());
        assertTrue(response.getBody().getMessage().contains("AuthenticationException"));
    }

    @Test
    void testHandleResponseStatusException() {
        // Arrange
        ResponseStatusException exception = new ResponseStatusException(
            HttpStatus.BAD_REQUEST, "Invalid request");

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleResponseStatusException(exception);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().getStatus());
        assertTrue(response.getBody().getMessage().contains("Invalid request"));
    }

    @Test
    void testHandleGenericException() {
        // Arrange
        Exception exception = new Exception("Some error occurred");

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleGenericException(exception);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(500, response.getBody().getStatus());
        assertEquals("Internal server error", response.getBody().getMessage());
    }

    @Test
    void testHandleAuthorizationDeniedExceptionWithEmptyMessage() {
        // Arrange
        AuthorizationDeniedException exception = new AuthorizationDeniedException("");

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleAuthorizationDeniedException(exception);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(403, response.getBody().getStatus());
    }

    @Test
    void testHandleResponseStatusExceptionWithNotFound() {
        // Arrange
        ResponseStatusException exception = new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Resource not found");

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleResponseStatusException(exception);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(404, response.getBody().getStatus());
        assertEquals("Resource not found", response.getBody().getMessage());
    }

    @Test
    void testHandleResponseStatusExceptionWithUnauthorized() {
        // Arrange
        ResponseStatusException exception = new ResponseStatusException(
            HttpStatus.UNAUTHORIZED, "Unauthorized access");

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleResponseStatusException(exception);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(401, response.getBody().getStatus());
    }

    @Test
    void testHandleGenericExceptionWithDifferentMessages() {
        // Test with different exception messages
        String[] messages = {"Error 1", "Error 2", "Error 3"};
        
        for (String message : messages) {
            Exception exception = new Exception(message);
            ResponseEntity<ErrorResponse> response = exceptionHandler.handleGenericException(exception);
            
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
            assertEquals(500, response.getBody().getStatus());
            assertEquals("Internal server error", response.getBody().getMessage());
        }
    }
}
