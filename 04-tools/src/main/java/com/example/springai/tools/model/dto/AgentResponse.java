package com.example.springai.tools.model.dto;

import java.util.List;

/**
 * Response DTO for agent task execution.
 */
public record AgentResponse(
    String answer,
    String sessionId,
    List<ToolExecutionInfo> toolExecutions,
    String status
) {
}
