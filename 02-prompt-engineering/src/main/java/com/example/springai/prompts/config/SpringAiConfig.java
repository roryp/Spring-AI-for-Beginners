package com.example.springai.prompts.config;

import com.openai.client.OpenAIClientAsync;
import org.springframework.ai.openaisdk.OpenAiSdkChatModel;
import org.springframework.ai.openaisdk.OpenAiSdkChatOptions;
import org.springframework.ai.openaisdk.setup.OpenAiSdkSetup;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for Spring AI with Azure OpenAI using the OpenAI SDK.
 * 
 * Note: For GPT-5 reasoning effort is controlled through prompt engineering
 * rather than model configuration parameters. See Gpt5PromptService for examples of how to
 * use prompts like "<reasoning_effort>low</reasoning_effort>" to control model behavior.
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
     * This single bean supports both synchronous and streaming operations.
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
     * Exposes the raw OpenAI async client for direct streaming.
     * The Spring AI SDK's stream() method uses collectList() which buffers
     * the entire response, destroying real-time token delivery. This bean
     * lets the streaming service bypass that and stream tokens as they arrive.
     */
    @Bean
    public OpenAIClientAsync openAIClientAsync() {
        return OpenAiSdkSetup.setupAsyncClient(
                azureEndpoint, azureApiKey, null, deploymentName,
                null, null, true, false, deploymentName,
                java.time.Duration.ofSeconds(60), 3, null, null);
    }
}
