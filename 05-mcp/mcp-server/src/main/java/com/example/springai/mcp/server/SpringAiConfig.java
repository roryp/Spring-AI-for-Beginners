package com.example.springai.mcp.server;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.openaisdk.OpenAiSdkChatModel;
import org.springframework.ai.openaisdk.OpenAiSdkChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for Spring AI with Azure OpenAI using the OpenAI SDK.
 * The ChatClient is used by TicTacToeTools to ask the LLM for strategic
 * tic-tac-toe moves when the aiMove tool is invoked.
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
    public ChatClient.Builder chatClientBuilder(ChatModel chatModel) {
        return ChatClient.builder(chatModel);
    }
}
