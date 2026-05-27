package com.example.springai.mcp.client;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Pins the {@link AgentService#buildSnapshotMessage(String, String)} format
 * that the agent feeds into chat memory.
 *
 * The original snapshot was just the raw MCP JSON, e.g.
 * {@code "board":["","","","","O","","X","",""]}, and the LLM had to count
 * commas across nine slots to place each piece. That regularly drifted by one
 * cell (e.g. reporting X at position 7 when the board had X at 6). The
 * rebuilt snapshot exposes both an explicit position->occupant list and a
 * pre-rendered 3x3 grid so the model never has to count.
 */
class AgentServiceSnapshotTest {

    private static AgentService newAgentService() {
        // ChatClient.Builder uses a fluent API. RETURNS_SELF makes every
        // builder method that returns ChatClient.Builder return the same
        // mock, so the chain in AgentService's constructor resolves without
        // needing to stub each overload (notably the varargs-taking
        // defaultToolCallbacks, which is awkward to match with any()).
        ChatClient.Builder builder = mock(ChatClient.Builder.class, Mockito.RETURNS_SELF);
        when(builder.build()).thenReturn(mock(ChatClient.class));

        ToolCallbackProvider provider = mock(ToolCallbackProvider.class);
        when(provider.getToolCallbacks()).thenReturn(new ToolCallback[0]);

        return new AgentService(builder, provider, mock(ChatMemory.class), mock(GameService.class));
    }

    @Test
    @DisplayName("Snapshot places X at index 6 and O at index 4 in the visual grid")
    void rendersXAtBottomLeftAndOAtCenter() {
        AgentService agent = newAgentService();
        String state = "{\"gameId\":\"game-42\","
                + "\"board\":[\"\",\"\",\"\",\"\",\"O\",\"\",\"X\",\"\",\"\"],"
                + "\"status\":\"IN_PROGRESS\","
                + "\"winner\":null,"
                + "\"currentPlayer\":\"X\"}";

        String snapshot = agent.buildSnapshotMessage("game-42", state);

        // Per-cell facts (these are what the LLM should rely on)
        assertThat(snapshot).contains("6: X");
        assertThat(snapshot).contains("4: O");
        assertThat(snapshot).contains("0: empty");
        assertThat(snapshot).contains("1: empty");
        assertThat(snapshot).contains("2: empty");
        assertThat(snapshot).contains("3: empty");
        assertThat(snapshot).contains("5: empty");
        assertThat(snapshot).contains("7: empty");
        assertThat(snapshot).contains("8: empty");
        // Visual grid: X must be in the bottom-left row line (6 | 7 | 8 row),
        // i.e. "X | 7 | 8". O must be the centre of "3 | O | 5".
        assertThat(snapshot).contains("X | 7 | 8");
        assertThat(snapshot).contains("3 | O | 5");
        // Header fields
        assertThat(snapshot).contains("Game: game-42");
        assertThat(snapshot).contains("Status: IN_PROGRESS");
        assertThat(snapshot).contains("Winner: none");
        assertThat(snapshot).contains("Current player: X");
    }

    @Test
    @DisplayName("Snapshot reports won state with winner and game-over current player")
    void reportsWonState() {
        AgentService agent = newAgentService();
        String state = "{\"gameId\":\"game-7\","
                + "\"board\":[\"X\",\"X\",\"X\",\"O\",\"O\",\"\",\"\",\"\",\"\"],"
                + "\"status\":\"WON\","
                + "\"winner\":\"X\","
                + "\"currentPlayer\":null,"
                + "\"winningCells\":[0,1,2]}";

        String snapshot = agent.buildSnapshotMessage("game-7", state);

        assertThat(snapshot).contains("Status: WON");
        assertThat(snapshot).contains("Winner: X");
        assertThat(snapshot).contains("Current player: game over");
        assertThat(snapshot).contains("0: X");
        assertThat(snapshot).contains("1: X");
        assertThat(snapshot).contains("2: X");
    }

    @Test
    @DisplayName("Falls back to raw JSON when the payload is malformed")
    void fallsBackOnMalformedJson() {
        AgentService agent = newAgentService();
        String malformed = "not a json document";

        String snapshot = agent.buildSnapshotMessage("game-bad", malformed);

        assertThat(snapshot).contains("Game: game-bad");
        assertThat(snapshot).contains(malformed);
    }
}
