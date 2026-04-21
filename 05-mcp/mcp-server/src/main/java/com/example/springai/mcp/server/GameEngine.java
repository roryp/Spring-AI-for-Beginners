package com.example.springai.mcp.server;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

/**
 * Tic-Tac-Toe game engine managing board state, move validation, and win detection.
 * Games are stored in memory with thread-safe concurrent access.
 *
 * Board positions are numbered 0-8 in a 3x3 grid:
 *   0 | 1 | 2
 *   ---------
 *   3 | 4 | 5
 *   ---------
 *   6 | 7 | 8
 */
public class GameEngine {

    private static final int[][] WIN_PATTERNS = {
        {0, 1, 2}, {3, 4, 5}, {6, 7, 8}, // rows
        {0, 3, 6}, {1, 4, 7}, {2, 5, 8}, // columns
        {0, 4, 8}, {2, 4, 6}              // diagonals
    };

    private final Map<String, String[]> boards = new ConcurrentHashMap<>();
    private final Map<String, String> statuses = new ConcurrentHashMap<>();
    private final Map<String, String> winners = new ConcurrentHashMap<>();
    private final Map<String, String> currentPlayers = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String newGame() {
        String gameId = UUID.randomUUID().toString().substring(0, 8);
        String[] board = new String[9];
        Arrays.fill(board, "");
        boards.put(gameId, board);
        statuses.put(gameId, "IN_PROGRESS");
        winners.put(gameId, "");
        currentPlayers.put(gameId, "X");
        return toJson(gameId);
    }

    public String makeMove(String gameId, int position, String player) {
        validateGame(gameId);
        String[] board = boards.get(gameId);

        if (!"IN_PROGRESS".equals(statuses.get(gameId))) {
            throw new IllegalStateException("Game is already over");
        }
        if (position < 0 || position > 8) {
            throw new IllegalArgumentException("Position must be 0-8, got: " + position);
        }
        if (!board[position].isEmpty()) {
            throw new IllegalArgumentException("Position " + position + " is already occupied");
        }
        if (!currentPlayers.get(gameId).equals(player)) {
            throw new IllegalArgumentException(
                    "It's " + currentPlayers.get(gameId) + "'s turn, not " + player);
        }

        board[position] = player;

        String winner = checkWinner(board);
        if (winner != null) {
            statuses.put(gameId, "WON");
            winners.put(gameId, winner);
            currentPlayers.put(gameId, "");
        } else if (isBoardFull(board)) {
            statuses.put(gameId, "DRAW");
            currentPlayers.put(gameId, "");
        } else {
            currentPlayers.put(gameId, player.equals("X") ? "O" : "X");
        }

        return toJson(gameId);
    }

    public String getBoardState(String gameId) {
        validateGame(gameId);
        return toJson(gameId);
    }

    public String getAvailableMoves(String gameId) {
        validateGame(gameId);
        String[] board = boards.get(gameId);
        List<Integer> available = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            if (board[i].isEmpty()) {
                available.add(i);
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("gameId", gameId);
        result.put("availableMoves", available);
        return writeJson(result);
    }

    private void validateGame(String gameId) {
        if (!boards.containsKey(gameId)) {
            throw new IllegalArgumentException("Game not found: " + gameId);
        }
    }

    private String checkWinner(String[] board) {
        for (int[] pattern : WIN_PATTERNS) {
            String a = board[pattern[0]];
            String b = board[pattern[1]];
            String c = board[pattern[2]];
            if (!a.isEmpty() && a.equals(b) && b.equals(c)) {
                return a;
            }
        }
        return null;
    }

    private boolean isBoardFull(String[] board) {
        for (String cell : board) {
            if (cell.isEmpty()) return false;
        }
        return true;
    }

    private String toJson(String gameId) {
        String[] board = boards.get(gameId);
        String winner = winners.get(gameId);
        String currentPlayer = currentPlayers.get(gameId);

        Map<String, Object> state = new LinkedHashMap<>();
        state.put("gameId", gameId);
        state.put("board", board);
        state.put("status", statuses.get(gameId));
        state.put("winner", winner.isEmpty() ? null : winner);
        state.put("currentPlayer", currentPlayer.isEmpty() ? null : currentPlayer);

        // Include winning cells if game is won
        if ("WON".equals(statuses.get(gameId))) {
            state.put("winningCells", findWinningCells(board));
        }

        return writeJson(state);
    }

    private List<Integer> findWinningCells(String[] board) {
        for (int[] pattern : WIN_PATTERNS) {
            String a = board[pattern[0]];
            String b = board[pattern[1]];
            String c = board[pattern[2]];
            if (!a.isEmpty() && a.equals(b) && b.equals(c)) {
                return List.of(pattern[0], pattern[1], pattern[2]);
            }
        }
        return List.of();
    }

    private String writeJson(Map<String, Object> data) {
        try {
            return objectMapper.writeValueAsString(data);
        } catch (JacksonException e) {
            throw new RuntimeException("Failed to serialize game state", e);
        }
    }
}
