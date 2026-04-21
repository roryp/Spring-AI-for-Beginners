package com.example.springai.tools.model.dto;

import java.util.List;

/**
 * Information about a tool execution.
 */
public record ToolExecutionInfo(
    String toolName,
    List<String> arguments,
    String status
) {
    /**
     * Create with execution time (legacy compat).
     */
    public ToolExecutionInfo(String toolName, String arguments, String result, long executionTimeMs) {
        this(toolName, List.of(arguments), result);
    }
}
