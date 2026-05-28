package com.example.springai.mcp.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.ai.tool.metadata.ToolMetadata;
import org.springframework.stereotype.Service;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AgentService - LLM-orchestrated MCP tool calling with conversation memory
 *
 * This service demonstrates the second client-side pattern for MCP:
 * the LLM decides when and how to invoke MCP tools.
 *
 * Compare with {@link GameService}:
 * - {@link GameService} uses {@code ToolCallback.call(...)} for direct,
 *   deterministic invocation. The client decides which tool to call.
 * - {@link AgentService} hands the same MCP tools to a {@link ChatClient}
 *   and lets the LLM choose. The model reads the tool descriptions
 *   discovered from the MCP server and autonomously calls them in a
 *   ReAct-style loop until it can answer.
 *
 * The {@link ToolCallbackProvider} injected here is the same one used by
 * {@link GameService} — Spring AI auto-wires it from the MCP server
 * connections declared in application.yaml.
 *
 * Conversation memory is wired through {@link MessageChatMemoryAdvisor}
 * (the same pattern used in
 * <a href="../../../../../../../../../../01-introduction/src/main/java/com/example/springai/service/ConversationService.java">Module 01's ConversationService</a>).
 * Each call passes a {@code conversationId} so the advisor can fetch the
 * prior messages for that conversation, prepend them to the prompt, and
 * persist the new exchange — letting the model remember the active gameId
 * across turns.
 *
 * Tool result inspection (see {@link #wrapWithGameIdCapture}) decorates
 * every discovered {@link ToolCallback} so the JSON returned by an MCP
 * tool is parsed for a {@code gameId} field and recorded against the
 * active conversation via the {@link ToolContext} carrying its
 * {@code conversationId}. This is the idiomatic Spring AI way to react to
 * tool results without scraping the model's prose.
 *
 * 💡 Ask GitHub Copilot:
 * - "How does ChatClient know to invoke MCP tools when I pass a ToolCallbackProvider?"
 * - "What happens under the hood when the LLM emits a tool_calls response — who executes the MCP call?"
 * - "How does MessageChatMemoryAdvisor isolate one browser's conversation from another?"
 * - "How would I restrict which discovered MCP tools the agent is allowed to use?"
 * - "What's the difference between defaultToolCallbacks(...) and toolCallbacks(...) per-prompt?"
 */
@Service
public class AgentService {

    private static final Logger log = LoggerFactory.getLogger(AgentService.class);

    /** Key used to pass the active conversation id into the tool-callback decorator via {@link ToolContext}. */
    private static final String CONVERSATION_ID_KEY = "conversationId";

    // The MCP server publishes its tools (and their descriptions, parameters
    // and return contracts) over the protocol; Spring AI forwards that schema
    // to the LLM on every request via .defaultToolCallbacks(...) below. So
    // this prompt deliberately does NOT name any tools — that's the whole
    // point of MCP discovery. We only describe the game and the behaviours we
    // want; the model picks the right tool for each behaviour by reading the
    // descriptions it discovered.
    private static final String SYSTEM_PROMPT = """
            You are a helpful assistant for a Tic-Tac-Toe game. A remote MCP
            server has published tools for starting games, making moves, and
            inspecting board state — call whichever ones you need to fulfil
            the user's request.

            Game rules:
            - The human always plays X and moves first.
            - The server's AI plays O. After every human move, if the game is
              still in progress, let the server's AI take its turn before
              replying to the user.
            - Only start a new game when the user explicitly asks to start,
              restart, or reset. For every other request, reuse the gameId
              from this conversation — never start a new game just to answer
              a question about the existing one.

            Answering rules:
            - The conversation history may contain one or more recent
              assistant messages labelled "[Board snapshot]". Each snapshot
              records the live server state of the active game at that
              moment. The MOST RECENT snapshot is ground truth; ignore any
              earlier snapshot or move history that disagrees with it.
            - If no snapshot is present, or you need details a snapshot does
              not cover, call the server for the current state before
              answering. Never guess.
            - When the most recent snapshot shows the game is finished (won
              or drawn), report that outcome plainly — do not say the game
              is still in progress.
            - Summarise the final board state in plain English, including
              whose turn is next or who won.
            """;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ChatClient chatClient;
    private final ChatMemory chatMemory;
    private final GameService gameService;
    /** Most recent gameId per conversation. Populated by the tool-result decorator
     *  ({@link #wrapWithGameIdCapture}) and by {@link #seedGameContext}. */
    private final Map<String, String> conversationGameIds = new ConcurrentHashMap<>();

    public AgentService(ChatClient.Builder chatClientBuilder,
                        ToolCallbackProvider mcpToolCallbacks,
                        ChatMemory chatMemory,
                        GameService gameService) {
        this.chatMemory = chatMemory;
        this.gameService = gameService;
        ToolCallback[] capturingCallbacks = Arrays.stream(mcpToolCallbacks.getToolCallbacks())
                .map(this::wrapWithGameIdCapture)
                .toArray(ToolCallback[]::new);
        this.chatClient = chatClientBuilder
                .defaultSystem(SYSTEM_PROMPT)
                .defaultTools(t -> t.callbacks(capturingCallbacks))
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();
        log.info("AgentService initialised with MCP tool callbacks: {}", capturingCallbacks.length);
    }

    /**
     * Sends a natural-language message to the LLM. The model reads the MCP tool
     * descriptions discovered from the server and decides which to call. Spring
     * AI executes the chosen tools transparently and feeds the results back to
     * the model until it produces a final answer.
     *
     * The {@code conversationId} keys the chat memory so prior turns in the
     * same conversation (most importantly, the active gameId) are reused.
     */
    public String chat(String conversationId, String userMessage) {
        return chat(conversationId, userMessage, null);
    }

    /**
     * Same as {@link #chat(String, String)} but accepts an optional
     * {@code boardGameId} hint from the visual board. The user may have
     * clicked the board between turns (started, played, won, or reset a
     * game) without the agent's knowledge, so whenever a {@code boardGameId}
     * is supplied we call the {@code getBoardState} MCP tool through
     * {@link GameService} and add the result to chat memory as a fresh
     * "[Board snapshot]" {@link AssistantMessage}. The model is instructed
     * (see {@link #SYSTEM_PROMPT}) to trust the most recent snapshot as
     * ground truth, so stale memory of an earlier board cannot leak into
     * the answer.
     *
     * <p>On every call, the active {@code conversationId} is propagated to
     * the tool callbacks via {@link ToolContext} so the result-capturing
     * decorator can attribute returned gameIds to the right conversation.</p>
     */
    public String chat(String conversationId, String userMessage, String boardGameId) {
        log.info("Agent [{}] received: {} (boardGameId={})", conversationId, userMessage, boardGameId);
        // Re-seed on every turn when there is an active visual-board game so the
        // model always sees the latest server state at the top of memory. The
        // user may have clicked the board, won, or reset between turns without
        // the agent's knowledge; only a fresh getBoardState call is
        // authoritative.
        if (boardGameId != null && !boardGameId.isBlank()) {
            seedGameContext(conversationId, boardGameId);
            conversationGameIds.put(conversationId, boardGameId);
        }
        String reply = chatClient.prompt()
                .user(userMessage)
                .tools(t -> t.context(CONVERSATION_ID_KEY, conversationId))
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .call()
                .content();
        log.info("Agent [{}] replied: {}", conversationId, reply);
        return reply;
    }

    /**
     * Bridge between the visual board and the agent without prompt injection.
     * Calls the same {@code getBoardState} MCP tool the LLM would call, then
     * records the result in chat memory as a fresh "[Board snapshot]"
     * assistant turn. The system prompt tells the model the latest snapshot
     * is ground truth, so this method runs on every chat call that carries a
     * {@code boardGameId} — ensuring the agent never answers from a stale
     * picture of the board.
     *
     * <p>The snapshot deliberately renders the board in two redundant forms
     * in addition to the raw JSON:
     * <ol>
     *   <li>An explicit {@code position: occupant} list, so the model never
     *       has to count commas in a 9-element JSON array (empty strings
     *       between commas are nearly invisible — the failure mode that
     *       caused the agent to misreport piece positions).</li>
     *   <li>A pre-rendered 3×3 grid with position numbers shown in empty
     *       cells, so the model can copy it verbatim into its reply.</li>
     * </ol>
     */
    private void seedGameContext(String conversationId, String gameId) {
        try {
            String state = gameService.getBoardState(gameId);
            String snapshot = buildSnapshotMessage(gameId, state);
            chatMemory.add(conversationId, new AssistantMessage(snapshot));
            log.info("Agent [{}] seeded snapshot for visual-board game {}", conversationId, gameId);
        } catch (Exception e) {
            log.warn("Agent [{}] failed to seed game context for {}", conversationId, gameId, e);
        }
    }

    /**
     * Renders the JSON game state returned by the {@code getBoardState} MCP
     * tool into an unambiguous "[Board snapshot]" assistant message. Falls
     * back to the raw JSON if the payload is malformed.
     */
    String buildSnapshotMessage(String gameId, String state) {
        try {
            JsonNode root = objectMapper.readTree(state);
            JsonNode boardNode = root.get("board");
            if (boardNode == null || !boardNode.isArray() || boardNode.size() != 9) {
                return rawSnapshot(gameId, state);
            }
            String[] cells = new String[9];
            for (int i = 0; i < 9; i++) {
                String value = boardNode.get(i).asString("");
                cells[i] = value == null ? "" : value;
            }
            String status = textOrNull(root.get("status"));
            String winner = textOrNull(root.get("winner"));
            String currentPlayer = textOrNull(root.get("currentPlayer"));

            StringBuilder sb = new StringBuilder();
            sb.append("[Board snapshot] Live server state of the active visual game.\n");
            sb.append("Game: ").append(gameId).append('\n');
            sb.append("Status: ").append(status == null ? "UNKNOWN" : status).append('\n');
            sb.append("Winner: ").append(winner == null ? "none" : winner).append('\n');
            sb.append("Current player: ")
                    .append(currentPlayer == null ? "game over" : currentPlayer)
                    .append('\n');
            sb.append("\nCell positions (position: occupant). Empty means the cell is unoccupied:\n");
            for (int i = 0; i < 9; i++) {
                sb.append("  ").append(i).append(": ")
                        .append(cells[i].isEmpty() ? "empty" : cells[i])
                        .append('\n');
            }
            sb.append("\nVisual board (rows top->bottom, columns left->right; position numbers shown for empty cells):\n");
            sb.append("  ").append(cellOrIndex(cells, 0))
                    .append(" | ").append(cellOrIndex(cells, 1))
                    .append(" | ").append(cellOrIndex(cells, 2)).append('\n');
            sb.append("  ---------\n");
            sb.append("  ").append(cellOrIndex(cells, 3))
                    .append(" | ").append(cellOrIndex(cells, 4))
                    .append(" | ").append(cellOrIndex(cells, 5)).append('\n');
            sb.append("  ---------\n");
            sb.append("  ").append(cellOrIndex(cells, 6))
                    .append(" | ").append(cellOrIndex(cells, 7))
                    .append(" | ").append(cellOrIndex(cells, 8)).append('\n');
            sb.append("\nRaw JSON: ").append(state);
            return sb.toString();
        } catch (Exception e) {
            log.debug("Could not parse board state JSON for snapshot, falling back to raw: {}",
                    e.getMessage());
            return rawSnapshot(gameId, state);
        }
    }

    private static String rawSnapshot(String gameId, String state) {
        return "[Board snapshot] Live server state of the active visual game.\n"
                + "Game: " + gameId + "\n"
                + "Current state: " + state;
    }

    private static String cellOrIndex(String[] cells, int position) {
        return cells[position].isEmpty() ? Integer.toString(position) : cells[position];
    }

    private static String textOrNull(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        String value = node.asString("");
        return (value == null || value.isEmpty()) ? null : value;
    }


    /**
     * Returns the most recent gameId the agent has been working with for this
     * conversation. Populated from authoritative sources: the
     * {@code boardGameId} hint passed through {@link #chat(String, String, String)}
     * and the {@code gameId} field returned by MCP tool calls (see
     * {@link #wrapWithGameIdCapture}). Empty until either has happened.
     */
    public Optional<String> activeGameId(String conversationId) {
        return Optional.ofNullable(conversationGameIds.get(conversationId));
    }

    /**
     * Clears the chat memory for the given conversation. Use this when the
     * user wants to start a fresh agent conversation (without affecting the
     * board-driven direct-call path).
     */
    public void clearConversation(String conversationId) {
        chatMemory.clear(conversationId);
        conversationGameIds.remove(conversationId);
        log.info("Cleared agent conversation [{}]", conversationId);
    }

    /**
     * Wraps a discovered MCP {@link ToolCallback} so its result is inspected
     * for a {@code gameId} field whenever the agent invokes it. The captured
     * id is attributed to the conversation passed through the
     * {@link ToolContext} (key {@value #CONVERSATION_ID_KEY}).
     *
     * <p>This replaces an earlier approach that required the model to surface
     * "Game: &lt;id&gt;" in its reply text and used a regex to scrape it back
     * out. The tool's JSON return value is the authoritative source — the
     * model's prose is not.</p>
     */
    private ToolCallback wrapWithGameIdCapture(ToolCallback delegate) {
        return new ToolCallback() {
            @Override
            public ToolDefinition getToolDefinition() {
                return delegate.getToolDefinition();
            }

            @Override
            public ToolMetadata getToolMetadata() {
                return delegate.getToolMetadata();
            }

            @Override
            public String call(String toolInput) {
                // Direct (non-agent) invocations do not carry a conversation id;
                // delegate without recording anything so GameService keeps working
                // unchanged if it ever uses these wrapped callbacks.
                return delegate.call(toolInput);
            }

            @Override
            public String call(String toolInput, ToolContext toolContext) {
                String result = delegate.call(toolInput, toolContext);
                Object convId = toolContext != null
                        ? toolContext.getContext().get(CONVERSATION_ID_KEY)
                        : null;
                if (convId instanceof String s && !s.isBlank()) {
                    captureGameId(s, delegate.getToolDefinition().name(), result);
                }
                return result;
            }
        };
    }

    /**
     * Parses an MCP tool result and, if it contains a {@code gameId} field,
     * records it against the conversation. Tool results are wrapped by the
     * MCP transport as a JSON array of TextContent objects
     * ({@code [{"text":"<actual JSON>"}]}); this method unwraps that envelope
     * before reading the payload.
     */
    private void captureGameId(String conversationId, String toolName, String toolResult) {
        if (toolResult == null || toolResult.isBlank()) {
            return;
        }
        try {
            JsonNode root = objectMapper.readTree(toolResult);
            JsonNode payload = root;
            if (root.isArray() && !root.isEmpty()) {
                JsonNode textNode = root.get(0).get("text");
                if (textNode != null) {
                    payload = objectMapper.readTree(textNode.asString());
                }
            }
            JsonNode gameIdNode = payload.get("gameId");
            if (gameIdNode == null || gameIdNode.isNull()) {
                return;
            }
            String gameId = gameIdNode.asString();
            if (gameId == null || gameId.isBlank()) {
                return;
            }
            String previous = conversationGameIds.put(conversationId, gameId);
            if (previous == null || !previous.equals(gameId)) {
                log.info("Agent [{}] captured gameId {} from {} tool result",
                        conversationId, gameId, toolName);
            }
        } catch (Exception e) {
            log.debug("Could not parse tool result from {} for [{}]: {}",
                    toolName, conversationId, e.getMessage());
        }
    }
}
