package com.example.springai.rag.service;

import com.example.springai.rag.config.RagSearchProperties;
import com.example.springai.rag.model.dto.RagRequest;
import com.example.springai.rag.model.dto.RagResponse;
import com.example.springai.rag.model.dto.SourceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * AdvisorRagService - Spring AI 2.0 Advisor-Based RAG Implementation
 * Run: ./start.sh (from module directory, after deploying Azure resources with azd up)
 * 
 * This service demonstrates the recommended Spring AI 2.0 approach to RAG
 * using the {@link QuestionAnswerAdvisor} with the {@link ChatClient} fluent API.
 * 
 * Compare this with {@link RagService} which implements the same RAG pipeline
 * manually (native approach) for educational purposes.
 * 
 * The Advisor approach:
 * 1. Automatically queries the VectorStore for relevant documents
 * 2. Injects retrieved context into the prompt
 * 3. Generates an answer grounded in the context
 * 
 * All of this happens in a single ChatClient call — no manual search,
 * context assembly, or prompt formatting required.
 * 
 * Key Concepts:
 * - ChatClient fluent API for building prompts
 * - QuestionAnswerAdvisor for automatic RAG
 * - SearchRequest for configuring similarity threshold and topK
 * - Advisor pattern for composable AI pipelines
 * 
 * 💡 Ask GitHub Copilot:
 * - "How does QuestionAnswerAdvisor inject context into the prompt?"
 * - "What's the difference between QuestionAnswerAdvisor and RetrievalAugmentationAdvisor?"
 * - "How can I customize the prompt template used by QuestionAnswerAdvisor?"
 * - "How do I add dynamic filter expressions at runtime?"
 */
@Service
public class AdvisorRagService {

    private static final Logger log = LoggerFactory.getLogger(AdvisorRagService.class);

    private final ChatClient chatClient;
    private final VectorStore vectorStore;
    private final RagSearchProperties searchProperties;

    public AdvisorRagService(ChatClient chatClient, VectorStore vectorStore, RagSearchProperties searchProperties) {
        this.chatClient = chatClient;
        this.vectorStore = vectorStore;
        this.searchProperties = searchProperties;
    }

    /**
     * Answer a question using Spring AI 2.0's QuestionAnswerAdvisor.
     * The advisor automatically handles vector search, context injection, and prompt augmentation.
     *
     * @param request RAG request with question
     * @return RAG response with answer and sources
     */
    public RagResponse ask(RagRequest request) {
        log.info("Processing advisor-based RAG request: '{}'", request.question());

        try {
            // Build QuestionAnswerAdvisor with search configuration
            QuestionAnswerAdvisor qaAdvisor = QuestionAnswerAdvisor.builder(vectorStore)
                    .searchRequest(searchProperties.buildSearchRequest())
                    .build();

            // Single call: ChatClient + Advisor handles search, context assembly, and generation
            ChatResponse chatResponse = chatClient.prompt()
                    .advisors(qaAdvisor)
                    .user(request.question())
                    .call()
                    .chatResponse();

            String answer = chatResponse.getResult().getOutput().getText();

            // Retrieve source documents for attribution
            List<SourceReference> sources = getSourceReferences(request.question());

            log.info("Advisor RAG response generated with {} sources", sources.size());

            return new RagResponse(answer, request.conversationId(), sources);

        } catch (Exception e) {
            log.error("Advisor RAG processing failed", e);
            throw new RuntimeException("Failed to process question: " + e.getMessage(), e);
        }
    }

    /**
     * Retrieve source references by running the same similarity search the advisor uses.
     * This provides source attribution for the UI.
     */
    private List<SourceReference> getSourceReferences(String question) {
        try {
            SearchRequest searchRequest = searchProperties.buildSearchRequest(question);

            List<Document> matches = vectorStore.similaritySearch(searchRequest);

            return matches.stream()
                    .map(doc -> {
                        Object filenameObj = doc.getMetadata().get("filename");
                        String filename = filenameObj != null ? filenameObj.toString() : "unknown";
                        double score = doc.getScore() != null ? doc.getScore() : 0.0;
                        return new SourceReference(filename, doc.getText(), score);
                    })
                    .toList();
        } catch (Exception e) {
            log.warn("Failed to retrieve source references", e);
            return new ArrayList<>();
        }
    }
}
