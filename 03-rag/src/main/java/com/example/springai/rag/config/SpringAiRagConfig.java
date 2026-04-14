package com.example.springai.rag.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.openaisdk.OpenAiSdkChatModel;
import org.springframework.ai.openaisdk.OpenAiSdkChatOptions;
import org.springframework.ai.openaisdk.OpenAiSdkEmbeddingModel;
import org.springframework.ai.openaisdk.OpenAiSdkEmbeddingOptions;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for Spring AI RAG components.
 * Provides beans for chat model, embedding model, and vector store.
 */
@Configuration
public class SpringAiRagConfig {

    @Value("${AZURE_OPENAI_ENDPOINT}")
    private String endpoint;

    @Value("${AZURE_OPENAI_API_KEY}")
    private String apiKey;

    @Value("${AZURE_OPENAI_DEPLOYMENT}")
    private String deployment;

    @Value("${AZURE_OPENAI_EMBEDDING_DEPLOYMENT}")
    private String embeddingDeployment;

    /**
     * Creates the OpenAI SDK Chat Model for answer generation.
     *
     * @return configured OpenAiSdkChatModel
     */
    @Bean
    public OpenAiSdkChatModel chatModel() {
        var chatOptions = OpenAiSdkChatOptions.builder()
                .baseUrl(endpoint)
                .apiKey(apiKey)
                .model(deployment)
                .azure(true)
                .build();

        return OpenAiSdkChatModel.builder()
                .options(chatOptions)
                .build();
    }

    /**
     * Creates a ChatClient for the advisor-based RAG approach.
     * ChatClient provides a fluent API for building prompts with advisors.
     *
     * @return configured ChatClient
     */
    @Bean
    public ChatClient chatClient(OpenAiSdkChatModel chatModel) {
        return ChatClient.builder(chatModel).build();
    }

    /**
     * Creates the OpenAI SDK Embedding Model for document vectorization.
     *
     * @return configured OpenAiSdkEmbeddingModel
     */
    @Bean
    public EmbeddingModel embeddingModel() {
        var embeddingOptions = OpenAiSdkEmbeddingOptions.builder()
                .baseUrl(endpoint)
                .apiKey(apiKey)
                .model(embeddingDeployment)
                .azure(true)
                .build();

        return new OpenAiSdkEmbeddingModel(embeddingOptions);
    }

    /**
     * Creates an in-memory vector store for development.
     * This store loses all data when the application restarts.
     *
     * @return in-memory vector store
     */
    @Bean
    public VectorStore vectorStore(EmbeddingModel embeddingModel) {
        return SimpleVectorStore.builder(embeddingModel).build();
    }
}
