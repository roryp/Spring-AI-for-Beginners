package com.example.springai.rag;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main Spring Boot application for RAG (Retrieval-Augmented Generation) module.
 * This module demonstrates document ingestion, embedding generation, vector storage,
 * and context-aware question answering.
 */
@SpringBootApplication
public class RagApplication {

    public static void main(String[] args) {
        SpringApplication.run(RagApplication.class, args);
    }
}
