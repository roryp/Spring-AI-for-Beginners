package com.example.springai.rag.model.dto;

/**
 * Represents a source reference in RAG responses.
 */
public record SourceReference(
    String filename,
    String excerpt,
    double relevanceScore
) {
}
