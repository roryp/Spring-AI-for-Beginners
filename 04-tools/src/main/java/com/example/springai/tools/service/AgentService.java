package com.example.springai.tools.service;

import com.example.springai.tools.model.dto.AgentRequest;
import com.example.springai.tools.model.dto.AgentResponse;
import com.example.springai.tools.tools.TemperatureTool;
import com.example.springai.tools.tools.WeatherTool;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AgentService - Tool-Calling Agent with Spring AI ChatClient
 * Run: ./start.sh (from module directory, after deploying Azure resources with azd up)
 * 
 * Agent service using Spring AI ChatClient with @Tool-annotated tool classes.
 * Demonstrates best practices:
 * - ChatClient with .tools() for passing @Tool-annotated instances
 * - Manual conversation memory with ConcurrentHashMap
 * - Spring AI handles the tool execution loop automatically
 * - Sliding window memory management
 *
 * 💡 Ask GitHub Copilot:
 * - "How does ChatClient.prompt().tools() discover @Tool-annotated methods?"
 * - "What happens during the tool execution loop when the model calls a function?"
 * - "How does the sliding window memory prevent exceeding the model's token limit?"
 * - "How can I customize which tools are available per request?"
 */
@Service
public class AgentService {

    private static final Logger log = LoggerFactory.getLogger(AgentService.class);
    private static final int MAX_MESSAGES = 20;

    private final ChatClient chatClient;
    private final WeatherTool weatherTool;
    private final TemperatureTool temperatureTool;
    private final Map<String, List<Message>> sessionMemories;

    /**
     * Constructor with auto-wired ChatClient.Builder.
     * Tool instances are created here and passed to ChatClient at call time.
     */
    public AgentService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
        this.weatherTool = new WeatherTool();
        this.temperatureTool = new TemperatureTool();
        this.sessionMemories = new ConcurrentHashMap<>();
        log.info("Agent service initialized with Spring AI ChatClient");
    }

    /**
     * Create a new agent session.
     * Returns a new UUID used to track conversation history.
     */
    public String createAgentSession() {
        String sessionId = UUID.randomUUID().toString();
        sessionMemories.put(sessionId, new ArrayList<>());
        log.info("Created new agent session: {}", sessionId);
        return sessionId;
    }

    /**
     * Execute an agent task using ChatClient with automatic tool orchestration.
     * The ChatClient handles the tool calling loop: when the model wants to call a tool,
     * Spring AI executes the @Tool method and feeds the result back to the model.
     */
    public AgentResponse executeTask(AgentRequest request) {
        log.info("Executing agent task: {}", request.message());

        String sessionId = request.sessionId();
        if (sessionId == null) {
            sessionId = createAgentSession();
        }

        try {
            List<Message> messages = sessionMemories.computeIfAbsent(
                sessionId, id -> new ArrayList<>()
            );

            // Add user message to history
            messages.add(new UserMessage(request.message()));

            // Trim to keep only the last MAX_MESSAGES
            if (messages.size() > MAX_MESSAGES) {
                List<Message> trimmed = new ArrayList<>(
                    messages.subList(messages.size() - MAX_MESSAGES, messages.size())
                );
                messages.clear();
                messages.addAll(trimmed);
            }

            // Call via ChatClient with tools — Spring AI handles tool execution loop
            String answer = chatClient.prompt()
                    .messages(messages)
                    .tools(weatherTool, temperatureTool)
                    .call()
                    .content();

            // Add AI response to history
            messages.add(new AssistantMessage(answer));

            log.info("Agent completed task successfully");

            return new AgentResponse(
                answer,
                sessionId,
                new ArrayList<>(),
                "completed"
            );

        } catch (Exception e) {
            log.error("Agent task execution failed", e);
            return new AgentResponse(
                "I encountered an error: " + e.getMessage(),
                sessionId,
                new ArrayList<>(),
                "failed"
            );
        }
    }

    /**
     * Simple chat for health checks.
     * Uses a dedicated session ID for health checks.
     */
    public String simpleChat(String message) {
        try {
            return chatClient.prompt()
                    .user(message)
                    .call()
                    .content();
        } catch (Exception e) {
            log.error("Simple chat failed", e);
            return "Error: " + e.getMessage();
        }
    }

    /**
     * Get available tools.
     */
    public List<String> getAvailableTools() {
        return List.of(
            "getCurrentWeather - Get current weather for a location",
            "getWeatherForecast - Get weather forecast",
            "celsiusToFahrenheit - Convert Celsius to Fahrenheit",
            "fahrenheitToCelsius - Convert Fahrenheit to Celsius",
            "celsiusToKelvin - Convert Celsius to Kelvin",
            "kelvinToCelsius - Convert Kelvin to Celsius",
            "fahrenheitToKelvin - Convert Fahrenheit to Kelvin",
            "kelvinToFahrenheit - Convert Kelvin to Fahrenheit"
        );
    }

    /**
     * Clear agent session.
     * Removes conversation history for the given session ID.
     */
    public void clearSession(String sessionId) {
        sessionMemories.remove(sessionId);
        log.info("Cleared session: {}", sessionId);
    }
}
