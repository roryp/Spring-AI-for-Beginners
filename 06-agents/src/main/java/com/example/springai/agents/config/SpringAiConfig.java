package com.example.springai.agents.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openaisdk.OpenAiSdkChatModel;
import org.springframework.ai.openaisdk.OpenAiSdkChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.springai.agents.advisor.AdvisorLogSink;
import com.example.springai.agents.advisor.MyLoggingAdvisor;

/**
 * Configuration for Spring AI with Azure OpenAI using the OpenAI SDK.
 * Uses the advisor pattern to attach a logging advisor to the ChatClient,
 * providing visibility into all LLM interactions.
 */
@Configuration
public class SpringAiConfig {

    @Value("${AZURE_OPENAI_ENDPOINT}")
    private String azureEndpoint;

    @Value("${AZURE_OPENAI_API_KEY}")
    private String azureApiKey;

    @Value("${AZURE_OPENAI_DEPLOYMENT}")
    private String deploymentName;

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

    @Bean
    public ChatClient chatClient(OpenAiSdkChatModel chatModel, AdvisorLogSink logSink) {
        return ChatClient.builder(chatModel)
                .defaultAdvisors(MyLoggingAdvisor.builder()
                        .showSystemMessage(true)
                        .showAvailableTools(true)
                        .labelPrefix("[Agent] ")
                        .logSink(logSink)
                        .build())
                .build();
    }
}
