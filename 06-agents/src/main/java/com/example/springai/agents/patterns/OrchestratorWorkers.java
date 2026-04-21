package com.example.springai.agents.patterns;

import java.util.List;

import org.springframework.ai.chat.client.AdvisorParams;
import org.springframework.ai.chat.client.ChatClient;

/**
 * Implements the Orchestrator-Workers pattern where a central LLM dynamically breaks
 * down complex tasks into subtasks, delegates them to worker LLMs, and synthesizes
 * the combined results.
 *
 * <p>When to use: Complex tasks where subtasks can't be predicted upfront and
 * require adaptive problem-solving.</p>
 *
 * 💡 Ask GitHub Copilot:
 * - "How is orchestrator-workers different from a fixed chain — when does the dynamic split pay off?"
 * - "How do I keep the orchestrator's JSON plan reliable and avoid parsing errors?"
 * - "Could workers run in parallel once the orchestrator has decomposed the task?"
 * - "How would I add a synthesis step that merges worker outputs into one coherent answer?"
 */
public class OrchestratorWorkers {

    public static final String DEFAULT_ORCHESTRATOR_PROMPT = """
            Analyze this task and break it down into 2-3 distinct approaches:

            Task: {task}

            Return your response in this JSON format:
            \\{
            "analysis": "Explain your understanding of the task and which variations would be valuable.
                         Focus on how each approach serves different aspects of the task.",
            "tasks": [
                \\{
                "type": "formal",
                "description": "Write a precise, technical version that emphasizes specifications"
                \\},
                \\{
                "type": "conversational",
                "description": "Write an engaging, friendly version that connects with readers"
                \\}
            ]
            \\}
            """;

    public static final String DEFAULT_WORKER_PROMPT = """
            Generate content based on:
            Task: {original_task}
            Style: {task_type}
            Guidelines: {task_description}
            """;

    public record Task(String type, String description) {
    }

    public record OrchestratorResponse(String analysis, List<Task> tasks) {
    }

    public record FinalResponse(String analysis, List<String> workerResponses) {
    }

    private final ChatClient chatClient;
    private final String orchestratorPrompt;
    private final String workerPrompt;

    public OrchestratorWorkers(ChatClient chatClient) {
        this(chatClient, DEFAULT_ORCHESTRATOR_PROMPT, DEFAULT_WORKER_PROMPT);
    }

    public OrchestratorWorkers(ChatClient chatClient, String orchestratorPrompt, String workerPrompt) {
        this.chatClient = chatClient;
        this.orchestratorPrompt = orchestratorPrompt;
        this.workerPrompt = workerPrompt;
    }

    /**
     * Processes a task using the orchestrator-workers pattern.
     *
     * @param taskDescription description of the task to be processed
     * @return FinalResponse containing the orchestrator's analysis and combined worker outputs
     */
    @SuppressWarnings("null")
    public FinalResponse process(String taskDescription) {
        // Step 1: Get orchestrator response
        OrchestratorResponse orchestratorResponse = this.chatClient.prompt()
                .user(u -> u.text(this.orchestratorPrompt)
                        .param("task", taskDescription))
                .advisors(AdvisorParams.ENABLE_NATIVE_STRUCTURED_OUTPUT)
                .call()
                .entity(OrchestratorResponse.class);

        // Step 2: Process each task with workers
        List<String> workerResponses = orchestratorResponse.tasks().stream()
                .map(task -> this.chatClient.prompt()
                        .user(u -> u.text(this.workerPrompt)
                                .param("original_task", taskDescription)
                                .param("task_type", task.type())
                                .param("task_description", task.description()))
                        .call()
                        .content())
                .toList();

        return new FinalResponse(orchestratorResponse.analysis(), workerResponses);
    }
}
