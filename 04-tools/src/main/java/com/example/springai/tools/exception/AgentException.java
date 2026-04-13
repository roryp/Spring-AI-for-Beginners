package com.example.springai.tools.exception;

/**
 * Custom exception for agent-related errors.
 * Following the error handling pattern from coding instructions.
 */
public class AgentException extends RuntimeException {
    
    public AgentException(String message) {
        super(message);
    }
    
    public AgentException(String message, Throwable cause) {
        super(message, cause);
    }
}
