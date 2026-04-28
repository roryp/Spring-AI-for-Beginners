package com.example.springai.config;

import org.springframework.context.annotation.Configuration;

/**
 * Configuration for Spring AI with Microsoft Foundry using the OpenAI SDK starter.
 *
 * The OpenAI SDK starter (spring-ai-starter-model-openai) auto-configures
 * OpenAiChatModel using properties from application.yaml:
 *
 *   spring.ai.openai.base-url  → AZURE_OPENAI_ENDPOINT
 *   spring.ai.openai.api-key   → AZURE_OPENAI_API_KEY
 *   spring.ai.openai.microsoft-deployment-name → AZURE_OPENAI_FAST_DEPLOYMENT
 *
 * Azure mode is detected automatically when the base URL contains openai.azure.com.
 *
 * 💡 Ask GitHub Copilot:
 * - "What exactly does the spring-ai-starter-model-openai auto-configure for me?"
 * - "How would I switch this app from Microsoft Foundry to OpenAI.com or a local model?"
 * - "When would I need to define an OpenAiChatModel bean manually instead of using the starter?"
 * - "How do I configure timeouts, retries, or a proxy for outbound model calls?"
 */
@Configuration
public class SpringAiConfig {
    // No manual bean needed — the starter auto-configures OpenAiChatModel.
}
