package com.example.springai.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ConversationService - Stateful Conversation Management
 * Run: ./start.sh (from module directory, after deploying Azure resources with azd up)
 *
 * Service for managing conversational interactions with memory.
 * Maintains separate conversation histories for different conversation IDs.
 *
 * This demonstrates the core difference between stateless and stateful AI:
 * - Without memory: Each request is independent, no context
 * - With memory: A sliding window of messages maintains conversation history
 *
 * Conversation history is managed automatically by {@link MessageChatMemoryAdvisor}
 * wired into {@link ChatClient}. The advisor reads the {@code conversationId} from
 * the request advisor params, fetches prior messages from the shared {@link ChatMemory}
 * bean, prepends them to the prompt, and persists the new exchange — no manual
 * add/get bookkeeping needed in this service.
 *
 * Key Concepts:
 * - ChatClient fluent API with advisor-driven memory
 * - MessageChatMemoryAdvisor + MessageWindowChatMemory for sliding-window history
 * - Per-conversation isolation via {@code ChatMemory.CONVERSATION_ID} advisor param
 *
 * 💡 Ask GitHub Copilot:
 * - "How does MessageChatMemoryAdvisor decide which messages to include in each call?"
 * - "Can I implement custom memory storage using a database instead of in-memory?"
 * - "What happens if I increase maxMessages beyond the model's token limit?"
 * - "How would I add summarization to compress old conversation history?"
 */
@Service
public class ConversationService {

    private final ChatClient chatClient;
    private final ChatMemory chatMemory;
    private final Set<String> activeConversations = ConcurrentHashMap.newKeySet();

    public ConversationService(ChatClient.Builder chatClientBuilder, ChatMemory chatMemory) {
        this.chatMemory = chatMemory;
        this.chatClient = chatClientBuilder
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();
    }

    /**
     * Start a new conversation and return a unique conversation ID.
     *
     * @return new conversation ID
     */
    public String startConversation() {
        String conversationId = UUID.randomUUID().toString();
        activeConversations.add(conversationId);
        return conversationId;
    }

    /**
     * Send a message within an existing conversation. {@link MessageChatMemoryAdvisor}
     * automatically loads prior messages for the given {@code conversationId}, sends
     * them along with the new user message, and persists the exchange.
     *
     * @param conversationId the conversation ID
     * @param message the user message
     * @return AI response
     */
    public String chat(String conversationId, String message) {
        activeConversations.add(conversationId);

        return chatClient.prompt()
                .user(message)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .call()
                .content();
    }

    /**
     * Get conversation history for a given conversation ID.
     *
     * @param conversationId the conversation ID
     * @return list of messages
     */
    public List<Message> getHistory(String conversationId) {
        return chatMemory.get(conversationId);
    }

    /**
     * Clear conversation history for a given conversation ID.
     *
     * @param conversationId the conversation ID
     */
    public void clearConversation(String conversationId) {
        chatMemory.clear(conversationId);
        activeConversations.remove(conversationId);
    }

    /**
     * Check if a conversation exists.
     *
     * @param conversationId the conversation ID
     * @return true if conversation exists
     */
    public boolean conversationExists(String conversationId) {
        return activeConversations.contains(conversationId);
    }
}
