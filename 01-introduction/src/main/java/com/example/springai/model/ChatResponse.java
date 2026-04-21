package com.example.springai.model;

import java.util.List;

/**
 * Response DTO for chat interactions.
 */
public record ChatResponse(
    String answer,
    String conversationId,
    List<String> sources,
    Integer tokenCount
) {
    public static ChatResponse of(String answer) {
        return new ChatResponse(answer, null, List.of(), null);
    }
    
    public static ChatResponse of(String answer, String conversationId) {
        return new ChatResponse(answer, conversationId, List.of(), null);
    }
}
