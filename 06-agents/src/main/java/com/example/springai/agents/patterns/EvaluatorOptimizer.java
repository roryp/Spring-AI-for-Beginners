package com.example.springai.agents.patterns;

import java.util.ArrayList;
import java.util.List;

import org.springframework.ai.chat.client.AdvisorParams;
import org.springframework.ai.chat.client.ChatClient;

/**
 * Implements the Evaluator-Optimizer workflow pattern where one LLM generates solutions
 * while another provides evaluation and feedback in an iterative loop.
 *
 * <p>When to use: Tasks with clear evaluation criteria where iterative refinement
 * provides measurable value (e.g., code generation, translation, content creation).</p>
 *
 * 💡 Ask GitHub Copilot:
 * - "What makes a good evaluator prompt — how do I avoid it being too lenient or too strict?"
 * - "How do I decide a max iteration count and stop conditions to avoid infinite refinement loops?"
 * - "Why is the evaluator a separate LLM call instead of self-critique in the generator?"
 * - "How would I plug in a non-LLM evaluator (unit tests, schema validation) for deterministic checks?"
 */
public class EvaluatorOptimizer {

    public static final String DEFAULT_GENERATOR_PROMPT = """
            Your goal is to complete the task based on the input. If there are feedback
            from your previous generations, you should reflect on them to improve your solution.

            CRITICAL: Your response must be a SINGLE LINE of valid JSON with NO LINE BREAKS except those
            explicitly escaped with \\n.
            Here is the exact format to follow, including all quotes and braces:

            {"thoughts":"Brief description here","response":"Your solution here with \\n for newlines"}

            Rules for the response field:
            1. ALL line breaks must use \\n
            2. ALL quotes must use \\"
            3. ALL backslashes must be doubled: \\
            4. NO actual line breaks or formatting - everything on one line

            Follow this format EXACTLY - your response must be valid JSON on a single line.
            """;

    public static final String DEFAULT_EVALUATOR_PROMPT = """
            Evaluate this implementation for correctness, quality, and best practices.
            Respond with EXACTLY this JSON format on a single line:

            {"evaluation":"PASS, NEEDS_IMPROVEMENT, or FAIL", "feedback":"Your feedback here"}

            The evaluation field must be one of: "PASS", "NEEDS_IMPROVEMENT", "FAIL"
            Use "PASS" only if all criteria are met with no improvements needed.
            """;

    public record Generation(String thoughts, String response) {
    }

    public record EvaluationResponse(Evaluation evaluation, String feedback) {
        public enum Evaluation {
            PASS, NEEDS_IMPROVEMENT, FAIL
        }
    }

    public record RefinedResponse(String solution, List<Generation> chainOfThought) {
    }

    private final ChatClient chatClient;
    private final String generatorPrompt;
    private final String evaluatorPrompt;
    private final int maxIterations;

    public EvaluatorOptimizer(ChatClient chatClient) {
        this(chatClient, DEFAULT_GENERATOR_PROMPT, DEFAULT_EVALUATOR_PROMPT, 3);
    }

    public EvaluatorOptimizer(ChatClient chatClient, String generatorPrompt,
            String evaluatorPrompt, int maxIterations) {
        this.chatClient = chatClient;
        this.generatorPrompt = generatorPrompt;
        this.evaluatorPrompt = evaluatorPrompt;
        this.maxIterations = maxIterations;
    }

    /**
     * Initiates the evaluator-optimizer workflow for a given task.
     *
     * @param task the task or problem to be solved through iterative refinement
     * @return a RefinedResponse containing the final solution and chain of thought
     */
    public RefinedResponse loop(String task) {
        List<String> memory = new ArrayList<>();
        List<Generation> chainOfThought = new ArrayList<>();
        return loop(task, "", memory, chainOfThought, 0);
    }

    @SuppressWarnings("null")
    private RefinedResponse loop(String task, String context, List<String> memory,
            List<Generation> chainOfThought, int iteration) {

        Generation generation = generate(task, context);
        memory.add(generation.response());
        chainOfThought.add(generation);

        EvaluationResponse evaluationResponse = evaluate(generation.response(), task);

        if (evaluationResponse.evaluation().equals(EvaluationResponse.Evaluation.PASS)
                || iteration >= maxIterations - 1) {
            return new RefinedResponse(generation.response(), chainOfThought);
        }

        StringBuilder newContext = new StringBuilder();
        newContext.append("Previous attempts:");
        for (String m : memory) {
            newContext.append("\n- ").append(m);
        }
        newContext.append("\nFeedback: ").append(evaluationResponse.feedback());

        return loop(task, newContext.toString(), memory, chainOfThought, iteration + 1);
    }

    @SuppressWarnings("null")
    private Generation generate(String task, String context) {
        return chatClient.prompt()
                .user(u -> u.text("{prompt}\n{context}\nTask: {task}")
                        .param("prompt", this.generatorPrompt)
                        .param("context", context)
                        .param("task", task))
                .advisors(AdvisorParams.ENABLE_NATIVE_STRUCTURED_OUTPUT)
                .call()
                .entity(Generation.class);
    }

    @SuppressWarnings("null")
    private EvaluationResponse evaluate(String content, String task) {
        return chatClient.prompt()
                .user(u -> u.text("{prompt}\nOriginal task: {task}\nContent to evaluate: {content}")
                        .param("prompt", this.evaluatorPrompt)
                        .param("task", task)
                        .param("content", content))
                .advisors(AdvisorParams.ENABLE_NATIVE_STRUCTURED_OUTPUT)
                .call()
                .entity(EvaluationResponse.class);
    }
}
