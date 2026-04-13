package com.example.springai.app;

import com.example.springai.service.ConversationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST controller for conversational chat with memory.
 * Maintains conversation history across multiple requests.
 */
@RestController
@RequestMapping("/api/conversation")
public class ConversationController {

    private final ConversationService conversationService;

    public ConversationController(ConversationService conversationService) {
        this.conversationService = conversationService;
    }

    /**
     * Start a new conversation session.
     * Returns a unique conversation ID to be used in subsequent requests.
     *
     * @return conversation ID
     */
    @PostMapping("/start")
    public ResponseEntity<Map<String, String>> startConversation() {
        try {
            String conversationId = conversationService.startConversation();
            return ResponseEntity.ok(Map.of(
                "conversationId", conversationId,
                "message", "Conversation started successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage() != null ? e.getMessage() : "Unknown error"));
        }
    }

    /**
     * Send a message within an existing conversation.
     * The conversation history is maintained automatically.
     *
     * Example request:
     * {
     *   "conversationId": "uuid-here",
     *   "message": "What did I just ask you?"
     * }
     *
     * @param body request containing conversationId and message
     * @return AI response with conversation context
     */
    @PostMapping("/chat")
    public ResponseEntity<Map<String, Object>> chat(@RequestBody Map<String, String> body) {
        String conversationId = body.get("conversationId");
        String message = body.getOrDefault("message", "");

        if (conversationId == null || conversationId.isBlank()) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "conversationId is required"));
        }

        if (message.isBlank()) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "message cannot be empty"));
        }

        try {
            String answer = conversationService.chat(conversationId, message);
            return ResponseEntity.ok(Map.of(
                "conversationId", conversationId,
                "message", message,
                "answer", answer
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "conversationId", conversationId,
                    "error", e.getMessage() != null ? e.getMessage() : "Unknown error"
                ));
        }
    }

    /**
     * Get the conversation history for a given conversation ID.
     *
     * @param conversationId the conversation ID
     * @return list of messages in the conversation
     */
    @GetMapping("/{conversationId}/history")
    public ResponseEntity<Map<String, Object>> getHistory(@PathVariable String conversationId) {
        try {
            if (!conversationService.conversationExists(conversationId)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Conversation not found"));
            }

            var history = conversationService.getHistory(conversationId);
            return ResponseEntity.ok(Map.of(
                "conversationId", conversationId,
                "messageCount", history.size(),
                "messages", history
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage() != null ? e.getMessage() : "Unknown error"));
        }
    }

    /**
     * Clear/delete a conversation.
     *
     * @param conversationId the conversation ID to clear
     * @return success message
     */
    @DeleteMapping("/{conversationId}")
    public ResponseEntity<Map<String, String>> clearConversation(@PathVariable String conversationId) {
        try {
            conversationService.clearConversation(conversationId);
            return ResponseEntity.ok(Map.of(
                "message", "Conversation cleared successfully",
                "conversationId", conversationId
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage() != null ? e.getMessage() : "Unknown error"));
        }
    }

    /**
     * Health check endpoint.
     *
     * @return health status
     */
    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "ok", "service", "conversation");
    }
}
