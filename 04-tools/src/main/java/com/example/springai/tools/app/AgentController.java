package com.example.springai.tools.app;

import com.example.springai.tools.exception.AgentException;
import com.example.springai.tools.model.dto.AgentRequest;
import com.example.springai.tools.model.dto.AgentResponse;
import com.example.springai.tools.model.dto.ErrorResponse;
import com.example.springai.tools.service.AgentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for AI agent interactions with tool calling.
 * Follows the REST API design pattern from coding instructions.
 *
 * 💡 Ask GitHub Copilot:
 * - "How does the AgentService decide when to invoke a tool vs answer directly?"
 * - "What's a good strategy for limiting tool-call loops so the agent doesn't run away?"
 * - "How would I stream partial results and tool invocations back to the UI as they happen?"
 * - "How should I authorize the caller — each tool may have different required permissions?"
 */
@RestController
@RequestMapping("/api/agent")
public class AgentController {

    private static final Logger log = LoggerFactory.getLogger(AgentController.class);
    private static final int MAX_MESSAGE_LENGTH = 1000;

    private final AgentService agentService;

    public AgentController(AgentService agentService) {
        this.agentService = agentService;
    }

    /**
     * Start a new agent session.
     *
     * @return session ID
     */
    @PostMapping("/start")
    public ResponseEntity<Map<String, String>> startSession() {
        try {
            String sessionId = agentService.createAgentSession();
            return ResponseEntity.ok(Map.of(
                "sessionId", sessionId,
                "message", "Agent session started successfully"
            ));
        } catch (Exception e) {
            log.error("Failed to start agent session", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Execute an agent task.
     * Follows the Request/Response pattern from coding instructions.
     *
     * @param request task request with message and optional session ID
     * @return agent response with result and tool executions
     */
    @PostMapping("/execute")
    public ResponseEntity<?> executeTask(@RequestBody AgentRequest request) {
        log.info("Received agent task: {}", request.message());

        try {
            // Validate request (as per error handling guidelines)
            if (request.message() == null || request.message().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Invalid request", "Message cannot be empty"));
            }

            // Limit prompt length (security consideration)
            if (request.message().length() > MAX_MESSAGE_LENGTH) {
                return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Invalid request", 
                        String.format("Message too long (max %d characters)", MAX_MESSAGE_LENGTH)));
            }

            // Execute agent task
            AgentResponse response = agentService.executeTask(request);
            log.info("Task completed with status: {}", response.status());

            return ResponseEntity.ok(response);

        } catch (AgentException e) {
            log.error("Agent task execution failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Agent error", e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error during task execution", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Execution failed", "An unexpected error occurred"));
        }
    }

    /**
     * Chat with the agent (simplified conversational interface).
     * Provides input validation and security checks.
     *
     * @param request chat request with message and optional session ID
     * @return chat response with answer
     */
    @PostMapping("/chat")
    public ResponseEntity<?> chat(@RequestBody Map<String, String> request) {
        String sessionId = request.get("sessionId");
        String message = request.get("message");

        // Input validation (as per security guidelines)
        if (message == null || message.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Message cannot be empty"));
        }

        // Limit prompt length (security consideration)
        if (message.length() > MAX_MESSAGE_LENGTH) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", String.format("Message too long (max %d characters)", MAX_MESSAGE_LENGTH)));
        }

        try {
            AgentRequest agentRequest = new AgentRequest(message, sessionId, true);
            AgentResponse response = agentService.executeTask(agentRequest);
            
            return ResponseEntity.ok(Map.of(
                "sessionId", response.sessionId(),
                "answer", response.answer(),
                "toolsUsed", response.toolExecutions().size(),
                "status", response.status()
            ));
            
        } catch (Exception e) {
            log.error("Agent chat failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to process message"));
        }
    }

    /**
     * Get available tools.
     *
     * @return list of available tools
     */
    @GetMapping("/tools")
    public ResponseEntity<List<String>> getTools() {
        try {
            List<String> tools = agentService.getAvailableTools();
            return ResponseEntity.ok(tools);
        } catch (Exception e) {
            log.error("Failed to get tools", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Clear an agent session.
     *
     * @param sessionId session ID to clear
     * @return status message
     */
    @DeleteMapping("/session/{sessionId}")
    public ResponseEntity<Map<String, String>> clearSession(@PathVariable String sessionId) {
        try {
            agentService.clearSession(sessionId);
            log.info("Cleared session: {}", sessionId);
            
            return ResponseEntity.ok(Map.of(
                "message", "Session cleared successfully",
                "sessionId", sessionId
            ));
        } catch (Exception e) {
            log.error("Failed to clear session: {}", sessionId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Health check endpoint.
     * Required for all modules as per guidelines.
     * Tests Azure connectivity and service availability.
     *
     * @return health status with Azure connectivity check
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        try {
            // Test Azure connectivity with a simple call
            String testResponse = agentService.simpleChat("Say 'OK' if you're working");
            boolean azureConnected = testResponse != null && !testResponse.contains("error");
            
            return ResponseEntity.ok(Map.of(
                "status", "healthy",
                "service", "agents",
                "azureConnected", azureConnected,
                "timestamp", System.currentTimeMillis()
            ));
        } catch (Exception e) {
            log.error("Health check failed", e);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                    "status", "unhealthy",
                    "error", e.getMessage()
                ));
        }
    }
}
