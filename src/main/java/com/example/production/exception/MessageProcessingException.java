package com.example.production.exception;

import lombok.Getter;

@Getter
public class MessageProcessingException extends RuntimeException {
    
    private final String messageBody;
    
    public MessageProcessingException(String message, String messageBody, Throwable cause) {
        super(message, cause);
        this.messageBody = messageBody;
    }
    
    public MessageProcessingException(String message, String messageBody) {
        super(message);
        this.messageBody = messageBody;
    }
}