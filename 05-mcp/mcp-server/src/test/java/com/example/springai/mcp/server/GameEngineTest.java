package com.example.springai.mcp.server;

import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SuppressWarnings("unchecked")
class GameEngineTest {

    private GameEngine gameEngine;
    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        gameEngine = new GameEngine();
    }

    @Test
    @DisplayName("Should create a new game with empty board")
    void testNewGame() throws Exception {
        String result = gameEngine.newGame();
        Map<String, Object> state = mapper.readValue(result, Map.class);

        assertThat(state.get("gameId")).isNotNull();
        assertThat(state.get("status")).isEqualTo("IN_PROGRESS");
        assertThat(state.get("currentPlayer")).isEqualTo("X");
        assertThat(state.get("winner")).isNull();

        List<String> board = (List<String>) state.get("board");
        assertThat(board).hasSize(9);
        assertThat(board).allMatch(String::isEmpty);
    }

    @Test
    @DisplayName("Should allow X to make the first move")
    void testPlayerMove() throws Exception {
        Map<String, Object> game = parseJson(gameEngine.newGame());
        String gameId = (String) game.get("gameId");

        String result = gameEngine.makeMove(gameId, 4, "X");
        Map<String, Object> state = parseJson(result);

        List<String> board = (List<String>) state.get("board");
        assertThat(board.get(4)).isEqualTo("X");
        assertThat(state.get("currentPlayer")).isEqualTo("O");
        assertThat(state.get("status")).isEqualTo("IN_PROGRESS");
    }

    @Test
    @DisplayName("Should detect a win")
    void testWinDetection() throws Exception {
        Map<String, Object> game = parseJson(gameEngine.newGame());
        String gameId = (String) game.get("gameId");

        gameEngine.makeMove(gameId, 0, "X");
        gameEngine.makeMove(gameId, 3, "O");
        gameEngine.makeMove(gameId, 1, "X");
        gameEngine.makeMove(gameId, 4, "O");
        String result = gameEngine.makeMove(gameId, 2, "X"); // X wins top row

        Map<String, Object> state = parseJson(result);
        assertThat(state.get("status")).isEqualTo("WON");
        assertThat(state.get("winner")).isEqualTo("X");
        assertThat((List<Integer>) state.get("winningCells")).containsExactly(0, 1, 2);
    }

    @Test
    @DisplayName("Should detect a draw")
    void testDrawDetection() throws Exception {
        Map<String, Object> game = parseJson(gameEngine.newGame());
        String gameId = (String) game.get("gameId");

        // X O X
        // X X O
        // O X O
        gameEngine.makeMove(gameId, 0, "X");
        gameEngine.makeMove(gameId, 1, "O");
        gameEngine.makeMove(gameId, 2, "X");
        gameEngine.makeMove(gameId, 5, "O");
        gameEngine.makeMove(gameId, 3, "X");
        gameEngine.makeMove(gameId, 6, "O");
        gameEngine.makeMove(gameId, 4, "X");
        gameEngine.makeMove(gameId, 8, "O");
        String result = gameEngine.makeMove(gameId, 7, "X");

        Map<String, Object> state = parseJson(result);
        assertThat(state.get("status")).isEqualTo("DRAW");
        assertThat(state.get("winner")).isNull();
    }

    @Test
    @DisplayName("Should reject move on occupied position")
    void testOccupiedPosition() throws Exception {
        Map<String, Object> game = parseJson(gameEngine.newGame());
        String gameId = (String) game.get("gameId");

        gameEngine.makeMove(gameId, 4, "X");

        assertThatThrownBy(() -> gameEngine.makeMove(gameId, 4, "O"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already occupied");
    }

    @Test
    @DisplayName("Should return available moves")
    void testAvailableMoves() throws Exception {
        Map<String, Object> game = parseJson(gameEngine.newGame());
        String gameId = (String) game.get("gameId");

        gameEngine.makeMove(gameId, 0, "X");
        gameEngine.makeMove(gameId, 4, "O");

        String result = gameEngine.getAvailableMoves(gameId);
        Map<String, Object> state = parseJson(result);
        List<Integer> moves = (List<Integer>) state.get("availableMoves");

        assertThat(moves).containsExactly(1, 2, 3, 5, 6, 7, 8);
    }

    private Map<String, Object> parseJson(String json) throws Exception {
        return mapper.readValue(json, Map.class);
    }
}
