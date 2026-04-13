package com.example.springai.tools.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openaisdk.OpenAiSdkChatModel;
import org.springframework.ai.openaisdk.OpenAiSdkChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * SpringAiConfig - Configuration for Spring AI with Azure OpenAI and Tool Calling
 * Run: ./start.sh (from module directory, after deploying Azure resources with azd up)
 * 
 * Configuration for Spring AI with Azure OpenAI using the OpenAI SDK.
 * Tools are registered using Spring AI's @Tool and @ToolParam annotations
 * on WeatherTool and TemperatureTool classes, and passed to the ChatClient
 * at call time via .tools().
 * 
 * The OpenAI SDK supports Azure OpenAI endpoints, providing a unified
 * interface for both OpenAI and Azure OpenAI services.
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

    @Value("${AZURE_OPENAI_ENDPOINT}")
    private String azureEndpoint;

    @Value("${AZURE_OPENAI_API_KEY}")
    private String azureApiKey;

    @Value("${AZURE_OPENAI_DEPLOYMENT}")
    private String deploymentName;

    /**
     * Creates the OpenAI SDK chat model configured for Azure OpenAI.
     * 
     * @return configured OpenAiSdkChatModel
     */
    @Bean
    public OpenAiSdkChatModel openAiSdkChatModel() {
        var chatOptions = OpenAiSdkChatOptions.builder()
                .baseUrl(azureEndpoint)
                .apiKey(azureApiKey)
                .model(deploymentName)
                .azure(true)
                .build();

        return OpenAiSdkChatModel.builder()
                .options(chatOptions)
                .build();
    }

    /**
     * Creates a ChatClient.Builder from the chat model.
     * The ChatClient provides a fluent API for making tool-calling conversations.
     * Tools (WeatherTool, TemperatureTool) are passed at call time via .tools().
     * 
     * @param chatModel the configured chat model
     * @return ChatClient.Builder for creating ChatClient instances
     */
    @Bean
    public ChatClient.Builder chatClientBuilder(OpenAiSdkChatModel chatModel) {
        return ChatClient.builder(chatModel);
    }
}
