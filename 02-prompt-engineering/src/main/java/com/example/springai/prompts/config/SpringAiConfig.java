package com.example.springai.prompts.config;

import com.openai.client.OpenAIClientAsync;
import org.springframework.ai.openaisdk.setup.OpenAiSdkSetup;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for Spring AI with Azure OpenAI using the OpenAI SDK starter.
 * 
 * The starter (spring-ai-starter-model-openai-sdk) auto-configures OpenAiSdkChatModel
 * using properties from application.yaml. Azure mode is detected automatically
 * when the base URL contains openai.azure.com.
 * 
 * Note: For GPT-5 reasoning effort is controlled through prompt engineering
 * rather than model configuration parameters. See Gpt5PromptService for examples of how to
 * use prompts like "<reasoning_effort>low</reasoning_effort>" to control model behavior.
 *
 * 💡 Ask GitHub Copilot:
 * - "Why is reasoning_effort set via prompts here instead of as an API parameter?"
 * - "When does it make sense to expose multiple chat model beans with different configurations?"
 * - "How would I inject Azure AD (managed identity) credentials here instead of an API key?"
 * - "How do I test this configuration without making real Azure OpenAI calls?"
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
