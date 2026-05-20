package com.example.springai.mcp.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST controller for the Tic-Tac-Toe game API.
 * The frontend JavaScript calls these endpoints to manage games and make moves.
 *
 * Two client-side patterns are exposed side by side:
 * 1. Direct MCP tool calls (the main game)
 *    - POST /api/game/new       → starts a new game via MCP tool
 *    - POST /api/game/move       → player moves, then AI responds via LLM strategy
 *    - GET  /api/game/state/{id} → fetches current board state via MCP tool
 *
 * 2. LLM-orchestrated MCP tool calls (the agent panel)
 *    - POST /api/agent/chat      → sends a natural-language message + conversationId;
 *                                   the LLM decides which MCP tools to invoke and the
 *                                   chat-memory advisor remembers prior turns
 *    - POST /api/agent/reset     → clears the chat memory for a conversationId
 *
 * 💡 Ask GitHub Copilot:
 * - "Why does the REST controller call GameService instead of invoking MCP tools directly?"
 * - "When would I choose the /api/agent/chat path over /api/game/move?"
 * - "How would I stream the AI's move back to the browser with Server-Sent Events?"
 * - "How do I surface remote MCP errors as meaningful HTTP status codes?"
 */
@RestController
@RequestMapping("/api")
public class GameController {

    private static final Logger log = LoggerFactory.getLogger(GameController.class);

    private final GameService gameService;
    private final AgentService agentService;

    public GameController(GameService gameService, AgentService agentService) {
        this.gameService = gameService;
        this.agentService = agentService;
    }

    @PostMapping("/game/new")
    public ResponseEntity<Map<String, Object>> newGame() {
        try {
            String result = gameService.newGame();
            return ResponseEntity.ok(parseJson(result));
        } catch (Exception e) {
            log.error("Failed to start new game", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/game/move")
    public ResponseEntity<Map<String, Object>> playerMove(@RequestBody Map<String, Object> body) {
        String gameId = (String) body.get("gameId");
        int position = ((Number) body.get("position")).intValue();

        if (gameId == null || gameId.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "gameId is required"));
        }
        if (position < 0 || position > 8) {
            return ResponseEntity.badRequest().body(Map.of("error", "position must be 0-8"));
        }

        try {
            // Make the human player's move (X)
            String moveResult = gameService.playerMove(gameId, position);
            Map<String, Object> state = parseJson(moveResult);

            // If game is still in progress, let the AI make its move
            if ("IN_PROGRESS".equals(state.get("status"))) {
                log.info("AI is thinking for game {}...", gameId);
                String aiResult = gameService.aiMove(gameId);
                state = parseJson(aiResult);
            }

            return ResponseEntity.ok(state);
        } catch (Exception e) {
            log.error("Move failed for game {}", gameId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/game/state/{gameId}")
    public ResponseEntity<Map<String, Object>> getState(@PathVariable String gameId) {
        try {
            String result = gameService.getBoardState(gameId);
            return ResponseEntity.ok(parseJson(result));
        } catch (Exception e) {
            log.error("Failed to get state for game {}", gameId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * LLM-orchestrated MCP path. The user sends a natural-language message;
     * the LLM reads the MCP tool descriptions discovered from the server and
     * decides which tools to call. The {@code conversationId} keys the chat
     * memory so prior turns (including the active gameId) are remembered.
     *
     * If the agent has been working with a game in this conversation, the
     * response also includes the current gameId and board state so the
     * visual board can render exactly what the agent just did on the server
     * — keeping the two client paths in sync.
     */
    @PostMapping("/agent/chat")
    public ResponseEntity<Map<String, Object>> agentChat(@RequestBody Map<String, Object> body) {
        String message = (String) body.get("message");
        String conversationId = (String) body.get("conversationId");
        String boardGameId = (String) body.get("gameId");
        if (message == null || message.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "message is required"));
        }
        if (conversationId == null || conversationId.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "conversationId is required"));
        }
        try {
            String reply = agentService.chat(conversationId, message, boardGameId);
            Map<String, Object> response = new java.util.HashMap<>();
            response.put("reply", reply);
            agentService.activeGameId(conversationId).ifPresent(gameId -> {
                response.put("gameId", gameId);
                try {
                    response.put("gameState", parseJson(gameService.getBoardState(gameId)));
                } catch (Exception fetchEx) {
                    log.warn("Could not fetch board state for agent-tracked game {}", gameId, fetchEx);
                }
            });
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Agent chat failed for conversation {}", conversationId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Clears the chat memory for the given agent conversation. The frontend
     * calls this when the user clicks "Reset Chat" so the next message starts
     * with a fresh history.
     */
    @PostMapping("/agent/reset")
    public ResponseEntity<Map<String, Object>> agentReset(@RequestBody Map<String, Object> body) {
        String conversationId = (String) body.get("conversationId");
        if (conversationId == null || conversationId.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "conversationId is required"));
        }
        agentService.clearConversation(conversationId);
        return ResponseEntity.ok(Map.of("status", "cleared", "conversationId", conversationId));
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseJson(String json) {
        try {
            return new tools.jackson.databind.ObjectMapper().readValue(json, Map.class);
        } catch (Exception e) {
            return Map.of("raw", json);
        }
    }
}
