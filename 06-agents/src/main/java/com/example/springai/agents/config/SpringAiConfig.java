package com.example.springai.agents.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.springai.agents.advisor.AdvisorLogSink;
import com.example.springai.agents.advisor.MyLoggingAdvisor;

/**
 * Configuration for Spring AI with Microsoft Foundry using the OpenAI SDK starter.
 * The starter (spring-ai-starter-model-openai) auto-configures OpenAiChatModel
 * using properties from application.yaml.
 * Uses the advisor pattern to attach a logging advisor to the ChatClient,
 * providing visibility into all LLM interactions.
 */
@Configuration
public class SpringAiConfig {

    @Bean
    public ChatClient chatClient(OpenAiChatModel chatModel, AdvisorLogSink logSink) {
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
