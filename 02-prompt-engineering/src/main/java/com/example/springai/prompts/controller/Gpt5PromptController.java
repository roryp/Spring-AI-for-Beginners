package com.example.springai.prompts.controller;

import com.example.springai.prompts.service.Gpt5PromptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

/**
 * REST Controller demonstrating GPT-5 prompting patterns.
 * 
 * Test these endpoints to see different GPT-5 prompting techniques in action.
 *
 * 💡 Ask GitHub Copilot:
 * - "How do the eight endpoints map to the prompting patterns — which one should I call when?"
 * - "How would I stream a pattern's response as SSE so the UI can display tokens as they arrive?"
 * - "Which of these endpoints would benefit from structured output (JSON schema) instead of free text?"
 * - "How do I add validation on the request body so malformed prompts fail fast?"
 */
@RestController
@RequestMapping("/api/gpt5")
@CrossOrigin(origins = "*")
public class Gpt5PromptController {

    @Autowired
    private Gpt5PromptService gpt5Service;

    /**
     * Quick, focused response (low eagerness)
     * 
     * Example:
     * POST /api/gpt5/focused
     * {"problem": "What is 15% of 200?"}
     */
    @PostMapping("/focused")
    public ResponseEntity<PromptResponse> solveFocused(@RequestBody ProblemRequest request) {
        String result = gpt5Service.solveFocused(request.problem());
        return ResponseEntity.ok(new PromptResponse(result));
    }

    /**
     * Thorough, autonomous response (high eagerness)
     * 
     * Example:
     * POST /api/gpt5/autonomous
     * {"problem": "Design a caching strategy for a high-traffic REST API"}
     */
    @PostMapping("/autonomous")
    public ResponseEntity<PromptResponse> solveAutonomous(@RequestBody ProblemRequest request) {
        String result = gpt5Service.solveAutonomous(request.problem());
        return ResponseEntity.ok(new PromptResponse(result));
    }

    /**
     * Streaming thorough response (high eagerness) - Server-Sent Events
     * Tokens stream in real-time so you can see progress immediately.
     *
     * Example:
     * POST /api/gpt5/autonomous/stream
     * {"problem": "Design a caching strategy for a high-traffic REST API"}
     *
     * Use curl to test: curl -N -X POST http://localhost:8083/api/gpt5/autonomous/stream \
     *   -H "Content-Type: application/json" -d '{"problem": "your problem here"}'
     */
    @PostMapping(value = "/autonomous/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> solveAutonomousStreaming(@RequestBody ProblemRequest request) {
        return gpt5Service.solveAutonomousStreaming(request.problem());
    }

    /**
     * Task execution with progress updates
     * 
     * Example:
     * POST /api/gpt5/task
     * {"task": "Create a Spring Boot REST endpoint for user management"}
     */
    @PostMapping("/task")
    public ResponseEntity<PromptResponse> executeTask(@RequestBody TaskRequest request) {
        String result = gpt5Service.executeWithPreamble(request.task());
        return ResponseEntity.ok(new PromptResponse(result));
    }

    /**
     * Generate code with self-reflection
     * 
     * Example:
     * POST /api/gpt5/code
     * {"requirement": "Create a service that validates email addresses"}
     */
    @PostMapping("/code")
    public ResponseEntity<PromptResponse> generateCode(@RequestBody CodeRequest request) {
        String result = gpt5Service.generateCodeWithReflection(request.requirement());
        return ResponseEntity.ok(new PromptResponse(result));
    }

    /**
     * Analyze code with structured feedback
     * 
     * Example:
     * POST /api/gpt5/analyze
     * {"code": "public void process(String s) { return s.toLowerCase(); }"}
     */
    @PostMapping("/analyze")
    public ResponseEntity<PromptResponse> analyzeCode(@RequestBody CodeAnalysisRequest request) {
        String result = gpt5Service.analyzeCode(request.code());
        return ResponseEntity.ok(new PromptResponse(result));
    }

    /**
     * Multi-turn conversation
     * 
     * Example:
     * POST /api/gpt5/chat
     * {"message": "Tell me about Spring Boot", "sessionId": "user123"}
     */
    @PostMapping("/chat")
    public ResponseEntity<PromptResponse> chat(@RequestBody ChatRequest request) {
        String result = gpt5Service.continueConversation(
            request.message(), 
            request.sessionId()
        );
        return ResponseEntity.ok(new PromptResponse(result));
    }

    /**
     * Clear a specific chat session
     * 
     * Example:
     * DELETE /api/gpt5/chat/user123
     */
    @DeleteMapping("/chat/{sessionId}")
    public ResponseEntity<String> clearSession(@PathVariable String sessionId) {
        gpt5Service.clearSession(sessionId);
        return ResponseEntity.ok("Session cleared: " + sessionId);
    }

    /**
     * Generate constrained output
     * 
     * Example:
     * POST /api/gpt5/constrained
     * {"topic": "machine learning", "format": "bullet points", "maxWords": 100}
     */
    @PostMapping("/constrained")
    public ResponseEntity<PromptResponse> generateConstrained(@RequestBody ConstrainedRequest request) {
        String result = gpt5Service.generateConstrained(
            request.topic(),
            request.format(),
            request.maxWords()
        );
        return ResponseEntity.ok(new PromptResponse(result));
    }

    /**
     * Step-by-step reasoning
     * 
     * Example:
     * POST /api/gpt5/reason
     * {"problem": "If a train leaves NYC at 2pm going 60mph, and another leaves Boston at 3pm going 80mph, when do they meet?"}
     */
    @PostMapping("/reason")
    public ResponseEntity<PromptResponse> solveWithReasoning(@RequestBody ProblemRequest request) {
        String result = gpt5Service.solveWithReasoning(request.problem());
        return ResponseEntity.ok(new PromptResponse(result));
    }

    // ==================== STREAMING ENDPOINTS ====================

    /**
     * Streaming focused response (low eagerness) - Server-Sent Events
     */
    @PostMapping(value = "/focused/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> solveFocusedStreaming(@RequestBody ProblemRequest request) {
        return gpt5Service.solveFocusedStreaming(request.problem());
    }

    /**
     * Streaming task execution - Server-Sent Events
     */
    @PostMapping(value = "/task/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> executeTaskStreaming(@RequestBody TaskRequest request) {
        return gpt5Service.executeWithPreambleStreaming(request.task());
    }

    /**
     * Streaming code generation - Server-Sent Events
     */
    @PostMapping(value = "/code/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> generateCodeStreaming(@RequestBody CodeRequest request) {
        return gpt5Service.generateCodeWithReflectionStreaming(request.requirement());
    }

    /**
     * Streaming code analysis - Server-Sent Events
     */
    @PostMapping(value = "/analyze/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> analyzeCodeStreaming(@RequestBody CodeAnalysisRequest request) {
        return gpt5Service.analyzeCodeStreaming(request.code());
    }

    /**
     * Streaming multi-turn chat - Server-Sent Events
     */
    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chatStreaming(@RequestBody ChatRequest request) {
        return gpt5Service.continueConversationStreaming(
            request.message(),
            request.sessionId()
        );
    }

    /**
     * Streaming constrained output - Server-Sent Events
     */
    @PostMapping(value = "/constrained/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> generateConstrainedStreaming(@RequestBody ConstrainedRequest request) {
        return gpt5Service.generateConstrainedStreaming(
            request.topic(),
            request.format(),
            request.maxWords()
        );
    }

    /**
     * Streaming step-by-step reasoning - Server-Sent Events
     */
    @PostMapping(value = "/reason/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> solveWithReasoningStreaming(@RequestBody ProblemRequest request) {
        return gpt5Service.solveWithReasoningStreaming(request.problem());
    }

    // ==================== REQUEST/RESPONSE DTOs ====================

    public record ProblemRequest(String problem) {}

    public record TaskRequest(String task) {}

    public record CodeRequest(String requirement) {}

    public record CodeAnalysisRequest(String code) {}

    public record ChatRequest(String message, String sessionId) {}

    public record ConstrainedRequest(String topic, String format, int maxWords) {}

    public record PromptResponse(String result, long timestamp) {
        public PromptResponse(String result) {
            this(result, System.currentTimeMillis());
        }
    }
}
