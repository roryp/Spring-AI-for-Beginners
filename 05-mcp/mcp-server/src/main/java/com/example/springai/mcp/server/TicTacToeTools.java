package com.example.springai.mcp.server;

import java.util.LinkedHashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Service;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * TicTacToeTools - MCP Server Tools for Tic-Tac-Toe
 *
 * Exposes game operations as MCP tools using Spring AI's @McpTool annotation.
 * The MCP client discovers these tools automatically via the Streamable HTTP
 * protocol and can invoke them through the ToolCallbackProvider.
 *
 * Key Concepts:
 * - @McpTool annotation exposes methods as MCP tools
 * - @McpToolParam describes tool parameters for AI discovery
 * - Tools return JSON strings with game state
 * - The aiMove tool uses the LLM to choose the best strategic move
 * - Stateful game management via in-memory GameEngine
 *
 * 💡 Ask GitHub Copilot:
 * - "How does @McpTool expose a method over the MCP protocol — what does the starter auto-register?"
 * - "Why do these tools return JSON strings instead of typed objects?"
 * - "How would I add a new tool (e.g. undoMove) and have the client pick it up without any client changes?"
 * - "How do I keep the AI_STRATEGY_PROMPT robust when the model occasionally returns invalid positions?"
 */
@Service
public class TicTacToeTools {

    private static final Logger log = LoggerFactory.getLogger(TicTacToeTools.class);

    private static final String AI_STRATEGY_PROMPT = """
            You are a tic-tac-toe expert playing as O against a human player X.
            Analyze the board and choose the BEST strategic move.

            Strategy priorities:
            1. WIN: Complete a line of three O's if possible
            2. BLOCK: Prevent the opponent from completing three X's
            3. CENTER: Take position 4 (center) if available
            4. CORNERS: Take positions 0, 2, 6, or 8
            5. EDGES: Take positions 1, 3, 5, or 7

            CRITICAL: Respond with ONLY a single digit (0-8). No text, no explanation.
            """;

    /** Strategic fallback order: center, corners, edges. Used when the LLM picks an invalid/occupied cell. */
    private static final int[] FALLBACK_PRIORITY = {4, 0, 2, 6, 8, 1, 3, 5, 7};

    private final GameEngine gameEngine;
    private final ChatClient chatClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public TicTacToeTools(ChatClient.Builder chatClientBuilder) {
        this.gameEngine = new GameEngine();
        this.chatClient = chatClientBuilder.build();
    }

    @McpTool(description = "Start a new tic-tac-toe game. Returns the game ID and an empty 3x3 board. "
            + "Player X (human) always goes first.")
    public String startNewGame() {
        return gameEngine.newGame();
    }

    @McpTool(description = "Make a move on the tic-tac-toe board. "
            + "Returns the updated board state, game status (IN_PROGRESS, WON, or DRAW), "
            + "and the winner if applicable.")
    public String makeMove(
            @McpToolParam(description = "The game ID returned by startNewGame") String gameId,
            @McpToolParam(description = "Board position 0-8 (top-left=0, bottom-right=8)") int position,
            @McpToolParam(description = "Player symbol: X for human, O for AI") String player) {
        return gameEngine.makeMove(gameId, position, player);
    }

    @McpTool(description = "AI makes a strategic move as player O using LLM-powered analysis. "
            + "The server analyzes the board state, asks the LLM for the best position, "
            + "and executes the move. Returns the updated board state after the AI's move.")
    public String aiMove(
            @McpToolParam(description = "The game ID") String gameId) {
        String boardState = gameEngine.getBoardState(gameId);
        String availableMovesJson = gameEngine.getAvailableMoves(gameId);
        Set<Integer> available = parseAvailableMoves(availableMovesJson);

        log.info("AI analyzing board for game {}: {}", gameId, boardState);
        log.info("Available moves: {}", available);

        if (available.isEmpty()) {
            throw new IllegalStateException("No available moves for game " + gameId);
        }

        // Ask the LLM for the best strategic move
        String aiResponse = chatClient.prompt()
                .system(AI_STRATEGY_PROMPT)
                .user("Current board state: " + boardState
                        + "\nAvailable positions: " + available
                        + "\nChoose your position:")
                .call()
                .content();

        int position = selectMove(aiResponse, available);
        log.info("AI chooses position {} for game {}", position, gameId);

        return gameEngine.makeMove(gameId, position, "O");
    }

    @McpTool(description = "Get the current board state for a game. "
            + "Returns the board array, game status, current player, and winner if any.")
    public String getBoardState(
            @McpToolParam(description = "The game ID") String gameId) {
        return gameEngine.getBoardState(gameId);
    }

    @McpTool(description = "Get the list of available (empty) positions on the board. "
            + "Returns positions as numbers 0-8 that have not been played yet.")
    public String getAvailableMoves(
            @McpToolParam(description = "The game ID") String gameId) {
        return gameEngine.getAvailableMoves(gameId);
    }

    private int selectMove(String aiResponse, Set<Integer> available) {
        Integer pick = extractFirstDigit(aiResponse);
        if (pick != null && available.contains(pick)) {
            return pick;
        }

        log.warn("AI response '{}' invalid or position {} unavailable. Falling back. Available={}",
                aiResponse, pick, available);
        for (int p : FALLBACK_PRIORITY) {
            if (available.contains(p)) {
                return p;
            }
        }
        // Should be unreachable: aiMove rejects empty boards before this point.
        throw new IllegalStateException("No available moves to fall back to");
    }

    private Integer extractFirstDigit(String aiResponse) {
        if (aiResponse == null) return null;
        for (int i = 0; i < aiResponse.length(); i++) {
            char c = aiResponse.charAt(i);
            if (c >= '0' && c <= '8') {
                return c - '0';
            }
        }
        return null;
    }

    private Set<Integer> parseAvailableMoves(String availableMovesJson) {
        try {
            JsonNode node = objectMapper.readTree(availableMovesJson).get("availableMoves");
            if (node != null && node.isArray()) {
                Set<Integer> moves = new LinkedHashSet<>();
                for (JsonNode n : node) {
                    if (n.isInt()) moves.add(n.intValue());
                }
                return moves;
            }
        } catch (Exception e) {
            log.warn("Failed to parse availableMoves JSON: {}", availableMovesJson, e);
        }
        return Set.of();
    }
}
