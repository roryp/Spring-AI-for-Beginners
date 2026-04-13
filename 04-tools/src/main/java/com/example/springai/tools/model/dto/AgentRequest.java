package com.example.springai.tools.model.dto;

/**
 * Request DTO for agent task execution.
 */
public record AgentRequest(
    String message,
    String sessionId,
    boolean enableTools
) {
    public AgentRequest {
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("Message cannot be null or blank");
        }
    }
    
    public static AgentRequest of(String message) {
        return new AgentRequest(message, null, true);
    }
}
