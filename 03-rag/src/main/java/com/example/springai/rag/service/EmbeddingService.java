package com.example.springai.rag.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service for storing document embeddings in the vector store.
 */
@Service
public class EmbeddingService {

    private static final Logger log = LoggerFactory.getLogger(EmbeddingService.class);

    private final VectorStore vectorStore;

    public EmbeddingService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    /**
     * Store document segments in the vector store.
     * The VectorStore handles embedding generation automatically.
     *
     * @param segments list of document segments
     * @return number of segments stored
     */
    public int storeSegments(List<Document> segments) {
        log.info("Storing {} segments in vector store", segments.size());
        
        try {
            vectorStore.add(segments);
            
            log.info("Successfully stored {} segments", segments.size());
            return segments.size();
            
        } catch (Exception e) {
            log.error("Failed to store segments", e);
            throw new RuntimeException("Segment storage failed: " + e.getMessage(), e);
        }
    }
}
