package com.example.springai.mcp.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Service;

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

    private final GameEngine gameEngine;
    private final ChatClient chatClient;

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
        String availableMoves = gameEngine.getAvailableMoves(gameId);

        log.info("AI analyzing board for game {}: {}", gameId, boardState);
        log.info("Available moves: {}", availableMoves);

        // Ask the LLM for the best strategic move
        String aiResponse = chatClient.prompt()
                .system(AI_STRATEGY_PROMPT)
                .user("Current board state: " + boardState
                        + "\nAvailable positions: " + availableMoves
                        + "\nChoose your position:")
                .call()
                .content();

        int position = parseAiPosition(aiResponse, availableMoves);
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

    private int parseAiPosition(String aiResponse, String availableMovesJson) {
        try {
            String cleaned = aiResponse.trim().replaceAll("[^0-8]", "");
            if (!cleaned.isEmpty()) {
                return Integer.parseInt(cleaned.substring(0, 1));
            }
        } catch (NumberFormatException e) {
            log.warn("Failed to parse AI response '{}', falling back", aiResponse);
        }

        // Fallback: pick the first available move
        log.warn("AI response unparseable, using first available move");
        String moves = availableMovesJson.replaceAll("[^0-8]", "");
        if (!moves.isEmpty()) {
            return Character.getNumericValue(moves.charAt(0));
        }
        throw new IllegalStateException("No available moves to fall back to");
    }
}
