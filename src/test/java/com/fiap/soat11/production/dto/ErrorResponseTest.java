package com.fiap.soat11.production.dto;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ErrorResponseTest {

    @Test
    void testErrorResponseConstructor() {
        // Arrange
        String message = "Test error message";
        int status = 404;

        // Act
        ErrorResponse errorResponse = new ErrorResponse(message, status);

        // Assert
        assertNotNull(errorResponse);
        assertEquals(message, errorResponse.getMessage());
        assertEquals(status, errorResponse.getStatus());
    }

    @Test
    void testErrorResponseSettersAndGetters() {
        // Arrange
        ErrorResponse errorResponse = new ErrorResponse("Initial message", 500);

        // Act
        errorResponse.setMessage("Updated message");
        errorResponse.setStatus(400);

        // Assert
        assertEquals("Updated message", errorResponse.getMessage());
        assertEquals(400, errorResponse.getStatus());
    }

    @Test
    void testErrorResponseWithMultipleStatuses() {
        // Test different HTTP status codes
        int[] statuses = {400, 401, 403, 404, 500};
        
        for (int status : statuses) {
            ErrorResponse errorResponse = new ErrorResponse("Error", status);
            assertEquals(status, errorResponse.getStatus());
        }
    }

    @Test
    void testErrorResponseWithDifferentMessages() {
        // Test with different messages
        String[] messages = {
            "Bad request",
            "Unauthorized",
            "Forbidden",
            "Not found",
            "Internal server error"
        };
        
        for (String message : messages) {
            ErrorResponse errorResponse = new ErrorResponse(message, 500);
            assertEquals(message, errorResponse.getMessage());
        }
    }

    @Test
    void testErrorResponseWithNullMessage() {
        // Act
        ErrorResponse errorResponse = new ErrorResponse(null, 500);

        // Assert
        assertNull(errorResponse.getMessage());
        assertEquals(500, errorResponse.getStatus());
    }

    @Test
    void testErrorResponseWithEmptyMessage() {
        // Act
        ErrorResponse errorResponse = new ErrorResponse("", 404);

        // Assert
        assertEquals("", errorResponse.getMessage());
        assertEquals(404, errorResponse.getStatus());
    }

    @Test
    void testErrorResponseUpdateMessage() {
        // Arrange
        ErrorResponse errorResponse = new ErrorResponse("Original", 200);

        // Act
        errorResponse.setMessage("Modified");

        // Assert
        assertEquals("Modified", errorResponse.getMessage());
    }

    @Test
    void testErrorResponseUpdateStatus() {
        // Arrange
        ErrorResponse errorResponse = new ErrorResponse("Message", 200);

        // Act
        errorResponse.setStatus(503);

        // Assert
        assertEquals(503, errorResponse.getStatus());
    }

    @Test
    void testErrorResponseMultipleUpdates() {
        // Arrange
        ErrorResponse errorResponse = new ErrorResponse("Initial", 200);

        // Act
        errorResponse.setMessage("First update");
        errorResponse.setStatus(400);
        errorResponse.setMessage("Second update");
        errorResponse.setStatus(500);

        // Assert
        assertEquals("Second update", errorResponse.getMessage());
        assertEquals(500, errorResponse.getStatus());
    }

    @Test
    void testErrorResponseWithLongMessage() {
        // Arrange
        String longMessage = "This is a very long error message that contains multiple words and should be properly stored and retrieved without any issues whatsoever";

        // Act
        ErrorResponse errorResponse = new ErrorResponse(longMessage, 500);

        // Assert
        assertEquals(longMessage, errorResponse.getMessage());
    }
}
