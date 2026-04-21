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
 *
 * 💡 Ask GitHub Copilot:
 * - "What exactly does the spring-ai-starter-model-openai-sdk auto-configure for me?"
 * - "How would I switch this app from Azure OpenAI to OpenAI.com or a local model?"
 * - "When would I need to define an OpenAiSdkChatModel bean manually instead of using the starter?"
 * - "How do I configure timeouts, retries, or a proxy for outbound model calls?"
 */
@Configuration
public class SpringAiConfig {
    // No manual bean needed — the starter auto-configures OpenAiSdkChatModel.
}
