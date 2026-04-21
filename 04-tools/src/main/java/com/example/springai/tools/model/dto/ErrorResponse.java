package com.example.springai.tools.model.dto;

/**
 * Error response DTO.
 */
public record ErrorResponse(
    String error,
    String message
) {
}
