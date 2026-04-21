package com.example.springai.model;

/**
 * Request DTO for chat interactions.
 */
public record ChatRequest(
    String message,
    String conversationId
) {
    public ChatRequest {
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("Message cannot be null or blank");
        }
    }
    
    public static ChatRequest of(String message) {
        return new ChatRequest(message, null);
    }
}
