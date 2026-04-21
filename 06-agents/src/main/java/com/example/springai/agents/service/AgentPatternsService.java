package com.example.springai.agents.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import com.example.springai.agents.patterns.ChainWorkflow;
import com.example.springai.agents.patterns.EvaluatorOptimizer;
import com.example.springai.agents.patterns.OrchestratorWorkers;
import com.example.springai.agents.patterns.ParallelizationWorkflow;
import com.example.springai.agents.patterns.RoutingWorkflow;

/**
 * Service that exposes each agentic workflow pattern.
 * Each method demonstrates one of the five patterns
 *
 * 💡 Ask GitHub Copilot:
 * - "Which of the five patterns should I reach for first when building a new agent?"
 * - "How could I combine two patterns (e.g. routing + chain) in a single workflow?"
 * - "How do I add tracing/logging so I can debug which pattern path was taken?"
 * - "How would I expose these patterns as @Tool-callable functions for an outer agent?"
 */
@Service
public class AgentPatternsService {

    private final ChainWorkflow chainWorkflow;
    private final ParallelizationWorkflow parallelizationWorkflow;
    private final RoutingWorkflow routingWorkflow;
    private final OrchestratorWorkers orchestratorWorkers;
    private final EvaluatorOptimizer evaluatorOptimizer;

    public AgentPatternsService(ChatClient chatClient) {
        this.chainWorkflow = new ChainWorkflow(chatClient);
        this.parallelizationWorkflow = new ParallelizationWorkflow(chatClient);
        this.routingWorkflow = new RoutingWorkflow(chatClient);
        this.orchestratorWorkers = new OrchestratorWorkers(chatClient);
        this.evaluatorOptimizer = new EvaluatorOptimizer(chatClient);
    }

    /**
     * Chain Workflow: Processes input through a sequence of LLM calls where
     * each step transforms the output of the previous one.
     */
    public String runChainWorkflow(String input) {
        return chainWorkflow.chain(input);
    }

    /**
     * Parallelization Workflow: Runs the same prompt across multiple inputs
     * concurrently, returning all results.
     */
    public String runParallelizationWorkflow(String prompt, List<String> inputs) {
        List<String> results = parallelizationWorkflow.parallel(prompt, inputs, inputs.size());
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < inputs.size(); i++) {
            sb.append("### Input ").append(i + 1).append(": ").append(inputs.get(i)).append("\n\n");
            sb.append(results.get(i)).append("\n\n---\n\n");
        }
        return sb.toString();
    }

    /**
     * Routing Workflow: Classifies input and routes it to a specialized handler.
     */
    public String runRoutingWorkflow(String input) {
        Map<String, String> routes = new LinkedHashMap<>();
        routes.put("billing",
                "You are a billing specialist. Help resolve billing issues professionally. "
                + "Provide clear steps for resolution and reference relevant policies.");
        routes.put("technical",
                "You are a technical support engineer. Help solve technical problems systematically. "
                + "Ask clarifying questions if needed and provide step-by-step solutions.");
        routes.put("general",
                "You are a customer service representative. Help with general inquiries warmly. "
                + "Be helpful, empathetic, and provide comprehensive information.");

        return routingWorkflow.route(input, routes);
    }

    /**
     * Orchestrator-Workers: Breaks a complex task into subtasks, delegates to
     * specialized workers, and combines their results.
     */
    public String runOrchestratorWorkersWorkflow(String task) {
        OrchestratorWorkers.FinalResponse response = orchestratorWorkers.process(task);
        StringBuilder sb = new StringBuilder();
        sb.append("## Orchestrator Analysis\n\n").append(response.analysis()).append("\n\n");
        sb.append("## Worker Outputs\n\n");
        int i = 1;
        for (String workerResult : response.workerResponses()) {
            sb.append("### Worker ").append(i++).append("\n\n").append(workerResult).append("\n\n---\n\n");
        }
        return sb.toString();
    }

    /**
     * Evaluator-Optimizer: Iteratively generates and evaluates solutions until
     * the evaluator is satisfied.
     */
    public String runEvaluatorOptimizerWorkflow(String task) {
        EvaluatorOptimizer.RefinedResponse response = evaluatorOptimizer.loop(task);
        StringBuilder sb = new StringBuilder();
        sb.append("## Final Solution\n\n").append(response.solution()).append("\n\n");
        sb.append("## Chain of Thought (").append(response.chainOfThought().size()).append(" iterations)\n\n");
        int i = 1;
        for (EvaluatorOptimizer.Generation gen : response.chainOfThought()) {
            sb.append("### Iteration ").append(i++).append("\n");
            sb.append("**Thoughts:** ").append(gen.thoughts()).append("\n\n");
            sb.append("**Response:** ").append(gen.response()).append("\n\n---\n\n");
        }
        return sb.toString();
    }
}
