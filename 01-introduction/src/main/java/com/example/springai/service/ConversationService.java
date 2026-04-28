package com.example.springai.service;

import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;

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
 * Key Concepts:
 * - ChatMemory abstraction for conversation context management
 * - MessageWindowChatMemory with sliding window of recent messages
 * - Per-user conversation isolation via conversation IDs
 * - Spring AI UserMessage and AssistantMessage type safety
 * 
 * 💡 Ask GitHub Copilot:
 * - "How does the sliding window decide which messages to drop when it's full?"
 * - "Can I implement custom memory storage using a database instead of in-memory?"
 * - "What happens if I increase maxMessages beyond the model's token limit?"
 * - "How would I add summarization to compress old conversation history?"
 */
@Service
public class ConversationService {

    private final OpenAiChatModel chatModel;
    private final ChatMemory chatMemory;
    private final Set<String> activeConversations = ConcurrentHashMap.newKeySet();

    public ConversationService(OpenAiChatModel chatModel) {
        this.chatModel = chatModel;
        this.chatMemory = MessageWindowChatMemory.builder()
                .maxMessages(10)
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
     * Send a message within an existing conversation.
     *
     * @param conversationId the conversation ID
     * @param message the user message
     * @return AI response
     */
    public String chat(String conversationId, String message) {
        activeConversations.add(conversationId);

        // Add user message to memory
        chatMemory.add(conversationId, new UserMessage(message));

        // Generate response using conversation history from ChatMemory
        ChatResponse chatResponse = chatModel.call(new Prompt(chatMemory.get(conversationId)));
        String responseText = chatResponse.getResult().getOutput().getText();

        // Add AI response to memory
        chatMemory.add(conversationId, chatResponse.getResult().getOutput());

        return responseText;
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
