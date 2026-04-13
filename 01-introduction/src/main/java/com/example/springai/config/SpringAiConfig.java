package com.example.springai.config;

import org.springframework.ai.openaisdk.OpenAiSdkChatModel;
import org.springframework.ai.openaisdk.OpenAiSdkChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for Spring AI with Azure OpenAI using the OpenAI SDK.
 * 
 * The OpenAI SDK supports Azure OpenAI endpoints, providing a unified
 * interface for both OpenAI and Azure OpenAI services.
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
}
