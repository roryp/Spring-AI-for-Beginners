package com.example.springai.service;

import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openaisdk.OpenAiSdkChatModel;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
 * Key Concepts:
 * - Manual message list management for conversation context
 * - Per-user conversation isolation with ConcurrentHashMap
 * - Spring AI UserMessage and AssistantMessage type safety
 * - Automatic context window management with max message trimming
 * 
 * 💡 Ask GitHub Copilot:
 * - "How does the sliding window decide which messages to drop when it's full?"
 * - "Can I implement custom memory storage using a database instead of in-memory?"
 * - "What happens if I increase maxMessages beyond the model's token limit?"
 * - "How would I add summarization to compress old conversation history?"
 */
@Service
public class ConversationService {

    private final OpenAiSdkChatModel chatModel;
    private final Map<String, List<Message>> conversationMemories;
    private static final int MAX_MESSAGES = 10;

    public ConversationService(OpenAiSdkChatModel chatModel) {
        this.chatModel = chatModel;
        this.conversationMemories = new ConcurrentHashMap<>();
    }

    /**
     * Start a new conversation and return a unique conversation ID.
     *
     * @return new conversation ID
     */
    public String startConversation() {
        String conversationId = UUID.randomUUID().toString();
        conversationMemories.put(conversationId, new ArrayList<>());
        return conversationId;
    }

    /**
     * Send a message within an existing conversation.
     *
     * @param conversationId the conversation ID
     * @param message the user message
     * @return AI response
     */
    public String chat(String conversationId, String message) {
        List<Message> messages = conversationMemories.computeIfAbsent(
            conversationId,
            id -> new ArrayList<>()
        );

        // Add user message to history
        messages.add(new UserMessage(message));

        // Trim to keep only the last MAX_MESSAGES
        if (messages.size() > MAX_MESSAGES) {
            List<Message> trimmed = new ArrayList<>(messages.subList(messages.size() - MAX_MESSAGES, messages.size()));
            messages.clear();
            messages.addAll(trimmed);
        }

        // Generate response
        ChatResponse chatResponse = chatModel.call(new Prompt(messages));
        String responseText = chatResponse.getResult().getOutput().getText();

        // Add AI response to history
        messages.add(new AssistantMessage(responseText));

        return responseText;
    }

    /**
     * Get conversation history for a given conversation ID.
     *
     * @param conversationId the conversation ID
     * @return list of messages
     */
    public List<Message> getHistory(String conversationId) {
        List<Message> messages = conversationMemories.get(conversationId);
        return messages != null ? List.copyOf(messages) : List.of();
    }

    /**
     * Clear conversation history for a given conversation ID.
     *
     * @param conversationId the conversation ID
     */
    public void clearConversation(String conversationId) {
        conversationMemories.remove(conversationId);
    }

    /**
     * Check if a conversation exists.
     *
     * @param conversationId the conversation ID
     * @return true if conversation exists
     */
    public boolean conversationExists(String conversationId) {
        return conversationMemories.containsKey(conversationId);
    }
}
