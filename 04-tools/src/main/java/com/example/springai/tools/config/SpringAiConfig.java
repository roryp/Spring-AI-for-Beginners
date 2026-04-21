package com.example.springai.tools.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openaisdk.OpenAiSdkChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * SpringAiConfig - Configuration for Spring AI with Microsoft Foundry and Tool Calling
 * Run: ./start.sh (from module directory, after deploying Azure resources with azd up)
 * 
 * The OpenAI SDK starter (spring-ai-starter-model-openai-sdk) auto-configures
 * OpenAiSdkChatModel using properties from application.yaml.
 * Azure mode is detected automatically when the base URL contains openai.azure.com.
 * 
 * Tools are registered using Spring AI's @Tool and @ToolParam annotations
 * on WeatherTool and TemperatureTool classes, and passed to the ChatClient
 * at call time via .tools().
 * 
 * Key Concepts:
 * - @Tool annotation makes methods discoverable by the AI
 * - @ToolParam describes parameters so the AI knows what to pass
 * - ChatClient provides a fluent API for tool-calling conversations
 * - Tools are passed at call time via ChatClient.prompt().tools(...)
 * 
 * 💡 Ask GitHub Copilot:
 * - "How does ChatClient handle the tool-calling loop internally?"
 * - "Can the AI call multiple tools in sequence to solve complex problems?"
 * - "What happens if a tool throws an exception - how should I handle errors?"
 * - "How would I integrate a real API (like weather or currency) instead of mock data?"
 */
@Configuration
public class SpringAiConfig {

    /**
     * Creates a ChatClient.Builder from the auto-configured chat model.
     * The ChatClient provides a fluent API for making tool-calling conversations.
     * Tools (WeatherTool, TemperatureTool) are passed at call time via .tools().
     * 
     * @param chatModel the auto-configured chat model
     * @return ChatClient.Builder for creating ChatClient instances
     */
    @Bean
    public ChatClient.Builder chatClientBuilder(OpenAiSdkChatModel chatModel) {
        return ChatClient.builder(chatModel);
    }
}
