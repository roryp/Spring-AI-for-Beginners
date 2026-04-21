package com.example.springai.tools.exception;

/**
 * Exception thrown when tool execution fails.
 * Provides context about which tool failed.
 */
public class ToolExecutionException extends AgentException {
    
    private final String toolName;
    
    public ToolExecutionException(String toolName, String message) {
        super(String.format("Tool '%s' execution failed: %s", toolName, message));
        this.toolName = toolName;
    }
    
    public ToolExecutionException(String toolName, String message, Throwable cause) {
        super(String.format("Tool '%s' execution failed: %s", toolName, message), cause);
        this.toolName = toolName;
    }
    
    public String getToolName() {
        return toolName;
    }
}
