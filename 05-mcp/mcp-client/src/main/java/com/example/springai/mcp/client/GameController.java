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
 * Flow:
 * 1. POST /api/game/new       → starts a new game via MCP tool
 * 2. POST /api/game/move       → player moves, then AI responds via LLM strategy
 * 3. GET  /api/game/state/{id} → fetches current board state via MCP tool
 *
 * 💡 Ask GitHub Copilot:
 * - "Why does the REST controller call GameService instead of invoking MCP tools directly?"
 * - "How would I stream the AI's move back to the browser with Server-Sent Events?"
 * - "How do I surface remote MCP errors as meaningful HTTP status codes?"
 */
@RestController
@RequestMapping("/api/game")
public class GameController {

    private static final Logger log = LoggerFactory.getLogger(GameController.class);

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @PostMapping("/new")
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

    @PostMapping("/move")
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

    @GetMapping("/state/{gameId}")
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

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseJson(String json) {
        try {
            return new tools.jackson.databind.ObjectMapper().readValue(json, Map.class);
        } catch (Exception e) {
            return Map.of("raw", json);
        }
    }
}
