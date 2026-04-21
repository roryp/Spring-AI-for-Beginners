package com.example.springai.mcp.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * GameService - Tic-Tac-Toe Game Orchestration via MCP Tools
 *
 * This service demonstrates Spring AI 2's MCP client capabilities:
 *
 * - **ToolCallbackProvider** auto-discovers tools from the MCP server
 * - **ToolCallback.call()** invokes MCP tools directly — no LLM in the client
 * - All game logic AND AI strategy live on the MCP server
 * - The client is a thin orchestration layer calling remote tools
 *
 * Key Concepts:
 * - ToolCallbackProvider auto-discovers MCP server tools at startup
 * - ToolCallback.call() invokes MCP tools directly (no LLM needed on client)
 * - The server's aiMove tool handles LLM-powered strategy internally
 * - MCP Streamable HTTP protocol connects client to server
 *
 * 💡 Ask GitHub Copilot:
 * - "How does ToolCallbackProvider discover the server's tools at startup — what wire calls happen?"
 * - "Why does this client have no ChatClient — when would I add one back?"
 * - "How would I swap stdio transport for Streamable HTTP (or vice versa) without changing this code?"
 * - "How should I handle errors or timeouts coming back from a remote MCP tool call?"
 */
@Service
public class GameService {

    private static final Logger log = LoggerFactory.getLogger(GameService.class);

    private final Map<String, ToolCallback> mcpTools;

    public GameService(ToolCallbackProvider toolCallbackProvider) {
        this.mcpTools = new HashMap<>();
        for (ToolCallback cb : toolCallbackProvider.getToolCallbacks()) {
            mcpTools.put(cb.getToolDefinition().name(), cb);
            log.info("Discovered MCP tool: {}", cb.getToolDefinition().name());
        }
    }

    public String newGame() {
        log.info("Starting new game via MCP tool");
        return callTool("startNewGame", "{}");
    }

    public String playerMove(String gameId, int position) {
        log.info("Player X moves to position {} in game {}", position, gameId);
        String input = String.format(
                "{\"gameId\":\"%s\",\"position\":%d,\"player\":\"X\"}", gameId, position);
        return callTool("makeMove", input);
    }

    public String aiMove(String gameId) {
        log.info("Requesting AI move from MCP server for game {}", gameId);
        String input = String.format("{\"gameId\":\"%s\"}", gameId);
        return callTool("aiMove", input);
    }

    public String getBoardState(String gameId) {
        return callTool("getBoardState",
                String.format("{\"gameId\":\"%s\"}", gameId));
    }

    private String callTool(String name, String input) {
        ToolCallback tool = mcpTools.get(name);
        if (tool == null) {
            throw new IllegalStateException("MCP tool not found: " + name
                    + ". Available: " + mcpTools.keySet());
        }
        String raw = tool.call(input);
        // ToolCallback.call() returns a JSON array of TextContent objects: [{"text":"..."}]
        // Extract the inner text value which contains the actual game JSON
        return extractText(raw);
    }

    private String extractText(String toolResponse) {
        try {
            var mapper = new tools.jackson.databind.ObjectMapper();
            var tree = mapper.readTree(toolResponse);
            if (tree.isArray() && !tree.isEmpty()) {
                var textNode = tree.get(0).get("text");
                if (textNode != null) {
                    return textNode.asString();
                }
            }
        } catch (Exception e) {
            log.debug("Response is not wrapped TextContent, using as-is: {}", e.getMessage());
        }
        return toolResponse;
    }
}
