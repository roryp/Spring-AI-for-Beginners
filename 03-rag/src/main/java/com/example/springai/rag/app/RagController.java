package com.example.springai.rag.app;

import com.example.springai.rag.model.dto.ErrorResponse;
import com.example.springai.rag.model.dto.RagRequest;
import com.example.springai.rag.model.dto.RagResponse;
import com.example.springai.rag.service.AdvisorRagService;
import com.example.springai.rag.service.RagService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for RAG (Retrieval-Augmented Generation) queries.
 * Provides two endpoints:
 * - /api/rag/ask — Native RAG (manual pipeline, educational)
 * - /api/rag/advisor/ask — Advisor-based RAG (Spring AI 2.0 recommended approach)
 *
 * 💡 Ask GitHub Copilot:
 * - "Walk me through the difference between the native and advisor endpoints — what does each control?"
 * - "When would I prefer the native pipeline over the advisor approach in production?"
 * - "How would I stream the generated answer token-by-token back to the browser?"
 * - "How do I return source citations alongside the answer so users can verify claims?"
 */
@RestController
@RequestMapping("/api/rag")
public class RagController {

    private static final Logger log = LoggerFactory.getLogger(RagController.class);

    private final RagService ragService;
    private final AdvisorRagService advisorRagService;

    public RagController(RagService ragService, AdvisorRagService advisorRagService) {
        this.ragService = ragService;
        this.advisorRagService = advisorRagService;
    }

    /**
     * Ask a question using RAG.
     *
     * @param request RAG request with question
     * @return answer with sources
     */
    @PostMapping("/ask")
    public ResponseEntity<?> ask(@RequestBody RagRequest request) {
        log.info("Received RAG question: {}", request.question());

        try {
            // Validate request
            if (request.question() == null || request.question().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Invalid request", "Question cannot be empty"));
            }

            // Process RAG request
            RagResponse response = ragService.ask(request);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("RAG request failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Request failed", e.getMessage()));
        }
    }

    /**
     * Ask a question using the Spring AI 2.0 Advisor-based RAG approach.
     * Uses QuestionAnswerAdvisor with ChatClient for automatic context retrieval.
     *
     * @param request RAG request with question
     * @return answer with sources
     */
    @PostMapping("/advisor/ask")
    public ResponseEntity<?> advisorAsk(@RequestBody RagRequest request) {
        log.info("Received advisor RAG question: {}", request.question());

        try {
            if (request.question() == null || request.question().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Invalid request", "Question cannot be empty"));
            }

            RagResponse response = advisorRagService.ask(request);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Advisor RAG request failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Request failed", e.getMessage()));
        }
    }

    /**
     * Health check endpoint.
     *
     * @return health status
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("RAG service is healthy");
    }
}
