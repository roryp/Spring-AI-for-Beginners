package com.example.springai.mcp.client;

import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring AI configuration for the MCP client.
 *
 * Provides the chat-memory beans used by {@link AgentService} so the LLM
 * orchestrating MCP tool calls can remember context (notably the active
 * gameId) across turns within a conversation.
 *
 * This mirrors the same pattern shown in
 * <a href="../../../../../../../../../../01-introduction/src/main/java/com/example/springai/config/SpringAiConfig.java">Module 01's SpringAiConfig</a>:
 * an in-memory repository keyed by conversation ID, fronted by a sliding
 * window so the prompt never exceeds the model's token limit.
 *
 * The OpenAI SDK starter (spring-ai-starter-model-openai) auto-configures
 * the {@code ChatClient.Builder} from Microsoft Foundry credentials in
 * application.yaml — no extra config needed here for that.
 *
 * 💡 Ask GitHub Copilot:
 * - "How would I swap InMemoryChatMemoryRepository for a JDBC or Redis-backed store?"
 * - "What's the right maxMessages value when each turn may include a tool-call round-trip?"
 * - "How would I add summarization so the agent can remember more than the window holds?"
 */
@Configuration
public class SpringAiConfig {

    /**
     * In-memory store for conversation messages, keyed by conversation ID.
     * Loses all data on application restart — swap for JDBC/Redis in production.
     */
    @Bean
    public ChatMemoryRepository chatMemoryRepository() {
        return new InMemoryChatMemoryRepository();
    }

    /**
     * Sliding-window chat memory keeping the most recent {@code maxMessages}
     * per conversation. Older messages are dropped automatically so the
     * prompt stays inside the model's token budget.
     *
     * Each agent turn typically produces several messages (user, assistant
     * tool_calls, tool results, final assistant reply), so a window of 20
     * holds roughly the last 4–5 conversational turns worth of context.
     */
    @Bean
    public ChatMemory chatMemory(ChatMemoryRepository chatMemoryRepository) {
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(chatMemoryRepository)
                .maxMessages(20)
                .build();
    }
}
