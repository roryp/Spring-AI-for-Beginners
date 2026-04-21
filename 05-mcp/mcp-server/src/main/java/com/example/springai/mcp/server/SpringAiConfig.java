package com.example.springai.mcp.server;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for Spring AI with Microsoft Foundry using the OpenAI SDK starter.
 * The starter (spring-ai-starter-model-openai-sdk) auto-configures OpenAiSdkChatModel
 * using properties from application.yaml.
 * The ChatClient is used by TicTacToeTools to ask the LLM for strategic
 * tic-tac-toe moves when the aiMove tool is invoked.
 */
@Configuration
public class SpringAiConfig {

    @Bean
    public ChatClient.Builder chatClientBuilder(ChatModel chatModel) {
        return ChatClient.builder(chatModel);
    }
}
