package com.example.springai.rag.service;

import com.example.springai.rag.model.dto.RagRequest;
import com.example.springai.rag.model.dto.RagResponse;
import com.example.springai.rag.model.dto.SourceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.document.Document;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * RagService - Retrieval-Augmented Generation Implementation
 * Run: ./start.sh (from module directory, after deploying Azure resources with azd up)
 * 
 * Service for Retrieval-Augmented Generation (RAG).
 * Combines semantic search with LLM generation.
 * 
 * Handles the complete RAG workflow:
 * 1. Search for semantically similar document chunks (embedding is done by VectorStore)
 * 2. Build context from relevant chunks
 * 3. Generate answer grounded in retrieved context
 * 
 * Key Concepts:
 * - Embedding-based semantic search via Spring AI VectorStore
 * - Similarity scoring and threshold filtering
 * - Context window management
 * - Source attribution for answers
 * 
 * 💡 Ask GitHub Copilot:
 * - "How does similarity search work with embeddings and what determines the score?"
 * - "What similarity threshold should I use and how does it affect results?"
 * - "How do I handle cases where no relevant documents are found?"
 * - "How can I implement hybrid search combining embeddings with keyword matching?"
 */
@Service
public class RagService {

    private static final Logger log = LoggerFactory.getLogger(RagService.class);
    
    private static final int MAX_RESULTS = 5;
    private static final double MIN_SCORE = 0.0;

    private final OpenAiChatModel chatModel;
    private final VectorStore vectorStore;

    public RagService(
            OpenAiChatModel chatModel,
            VectorStore vectorStore) {
        this.chatModel = chatModel;
        this.vectorStore = vectorStore;
    }

    /**
     * Answer a question using retrieval-augmented generation.
     *
     * @param request RAG request with question
     * @return RAG response with answer and sources
     */
    public RagResponse ask(RagRequest request) {
        log.info("Processing RAG request: '{}'", request.question());

        try {
            // 1. Search for relevant document chunks (VectorStore handles embedding the query)
            SearchRequest searchRequest = SearchRequest.builder()
                .query(request.question())
                .topK(MAX_RESULTS)
                .similarityThreshold(MIN_SCORE)
                .build();
            
            List<Document> matches = vectorStore.similaritySearch(searchRequest);
            
            log.info("Found {} matches above MIN_SCORE {} for question", matches.size(), MIN_SCORE);
            matches.forEach(doc -> log.info("Match score: {}", 
                    String.format("%.4f", doc.getScore() != null ? doc.getScore() : 0.0)));
            
            if (matches.isEmpty()) {
                log.warn("No relevant documents found for question: '{}'", request.question());
                return new RagResponse(
                    "I cannot answer this question based on the provided documents. " +
                    "Please try asking something related to the uploaded content.",
                    request.conversationId(),
                    new ArrayList<>()
                );
            }
            
            // 2. Build context from retrieved segments
            String context = matches.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n\n"));
            
            // 3. Create prompt with context
            String promptText = String.format("""
                Answer the question based on the following context. 
                If the answer cannot be found in the context, say so.
                
                Context:
                %s
                
                Question: %s
                
                Answer:""", context, request.question());
            
            // 4. Generate answer
            String answer = chatModel.call(new Prompt(promptText))
                    .getResult().getOutput().getText();
            
            // 5. Build source references
            List<SourceReference> sources = matches.stream()
                .map(doc -> {
                    Object filenameObj = doc.getMetadata().get("filename");
                    String filename = filenameObj != null ? filenameObj.toString() : "unknown";
                    double score = doc.getScore() != null ? doc.getScore() : 0.0;
                    return new SourceReference(filename, doc.getText(), score);
                })
                .collect(Collectors.toList());

            log.info("RAG response generated with {} sources", sources.size());
            
            return new RagResponse(answer, request.conversationId(), sources);

        } catch (Exception e) {
            log.error("RAG processing failed", e);
            throw new RuntimeException("Failed to process question: " + e.getMessage(), e);
        }
    }
}
