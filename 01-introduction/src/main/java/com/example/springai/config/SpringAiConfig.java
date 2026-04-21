package com.example.springai.config;

import org.springframework.context.annotation.Configuration;

/**
 * Configuration for Spring AI with Azure OpenAI using the OpenAI SDK starter.
 *
 * The OpenAI SDK starter (spring-ai-starter-model-openai-sdk) auto-configures
 * OpenAiSdkChatModel using properties from application.yaml:
 *
 *   spring.ai.openai-sdk.base-url  → AZURE_OPENAI_ENDPOINT
 *   spring.ai.openai-sdk.api-key   → AZURE_OPENAI_API_KEY
 *   spring.ai.openai-sdk.microsoft-deployment-name → AZURE_OPENAI_DEPLOYMENT
 *
 * Azure mode is detected automatically when the base URL contains openai.azure.com.
 */
@Configuration
public class SpringAiConfig {
    // No manual bean needed — the starter auto-configures OpenAiSdkChatModel.
}
