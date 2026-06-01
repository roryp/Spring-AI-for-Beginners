package com.example.springai.tools.service;

import com.example.springai.tools.model.dto.AgentRequest;
import com.example.springai.tools.model.dto.AgentResponse;
import com.example.springai.tools.model.dto.ToolExecutionInfo;
import com.example.springai.tools.tools.TemperatureTool;
import com.example.springai.tools.tools.WeatherTool;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
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
 * - ChatMemory (MessageWindowChatMemory) for conversation context management
 * - Spring AI handles the tool execution loop automatically
 * - Sliding window memory management via MessageWindowChatMemory
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

    private final ChatClient chatClient;
    private final WeatherTool weatherTool;
    private final TemperatureTool temperatureTool;
    private final ToolExecutionInfo.Recorder toolExecutionRecorder;
    private final ChatMemory chatMemory;
    private final Set<String> activeSessions = ConcurrentHashMap.newKeySet();

    /**
     * Constructor with auto-wired ChatClient.Builder.
     * Tool instances are created here and passed to ChatClient at call time.
     */
    public AgentService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
        this.toolExecutionRecorder = new ToolExecutionInfo.Recorder();
        this.weatherTool = new WeatherTool(toolExecutionRecorder);
        this.temperatureTool = new TemperatureTool(toolExecutionRecorder);
        this.chatMemory = MessageWindowChatMemory.builder()
                .maxMessages(20)
                .build();
        log.info("Agent service initialized with Spring AI ChatClient");
    }

    /**
     * Create a new agent session.
     * Returns a new UUID used to track conversation history.
     */
    public String createAgentSession() {
        String sessionId = UUID.randomUUID().toString();
        activeSessions.add(sessionId);
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
            activeSessions.add(sessionId);
            toolExecutionRecorder.start();

            // Add user message to memory
            chatMemory.add(sessionId, new UserMessage(request.message()));

            // Get conversation history from ChatMemory
            List<Message> messages = chatMemory.get(sessionId);

            // Call via ChatClient with tools — Spring AI handles tool execution loop
            String answer = chatClient.prompt()
                    .messages(messages)
                    .tools(weatherTool, temperatureTool)
                    .call()
                    .content();

            // Add AI response to memory
            chatMemory.add(sessionId, new org.springframework.ai.chat.messages.AssistantMessage(answer));
            List<ToolExecutionInfo> toolExecutions = toolExecutionRecorder.stop();

            log.info("Agent completed task successfully with {} tool execution(s)", toolExecutions.size());

            return new AgentResponse(
                answer,
                sessionId,
                toolExecutions,
                "completed"
            );

        } catch (Exception e) {
            List<ToolExecutionInfo> toolExecutions = toolExecutionRecorder.stop();
            log.error("Agent task execution failed", e);
            return new AgentResponse(
                "I encountered an error: " + e.getMessage(),
                sessionId,
                toolExecutions,
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
        chatMemory.clear(sessionId);
        activeSessions.remove(sessionId);
        log.info("Cleared session: {}", sessionId);
    }
}
