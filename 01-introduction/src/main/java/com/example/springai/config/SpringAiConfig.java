package com.example.springai.config;

import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for Spring AI with Microsoft Foundry using the OpenAI SDK starter.
 *
 * The OpenAI SDK starter (spring-ai-starter-model-openai) auto-configures
 * {@code OpenAiChatModel} and a {@code ChatClient.Builder} from properties in
 * application.yaml:
 *
 *   spring.ai.openai.base-url  → AZURE_OPENAI_ENDPOINT
 *   spring.ai.openai.api-key   → AZURE_OPENAI_API_KEY
 *   spring.ai.openai.microsoft-deployment-name → AZURE_OPENAI_FAST_DEPLOYMENT
 *
 * Azure mode is detected automatically when the base URL contains openai.azure.com.
 *
 * This config adds chat-memory beans used by {@code ConversationService} to maintain
 * per-conversation history through {@code MessageChatMemoryAdvisor}.
 *
 * 💡 Ask GitHub Copilot:
 * - "What exactly does the spring-ai-starter-model-openai auto-configure for me?"
 * - "How would I switch this app from Microsoft Foundry to OpenAI.com or a local model?"
 * - "How does MessageChatMemoryAdvisor decide which conversation a request belongs to?"
 * - "How would I swap InMemoryChatMemoryRepository for a JDBC or Redis-backed store?"
 */
@Configuration
public class SpringAiConfig {

    /**
     * In-memory store for conversation messages. Each entry is keyed by conversation ID.
     * Loses all data on application restart — swap for JDBC/Redis in production.
     */
    @Bean
    public ChatMemoryRepository chatMemoryRepository() {
        return new InMemoryChatMemoryRepository();
    }

    /**
     * Sliding-window memory keeping the most recent {@code maxMessages} per conversation.
     * Older messages are automatically dropped so the prompt stays within model token limits.
     */
    @Bean
    public ChatMemory chatMemory(ChatMemoryRepository chatMemoryRepository) {
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(chatMemoryRepository)
                .maxMessages(10)
                .build();
    }
}
