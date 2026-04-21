package com.example.springai.agents.patterns;

import org.springframework.ai.chat.client.ChatClient;

/**
 * Implements the Prompt Chaining workflow pattern for decomposing complex tasks
 * into a sequence of LLM calls where each step processes the output of the previous one.
 *
 * <p>When to use: Tasks that can be cleanly decomposed into fixed subtasks.
 * Trades latency for higher accuracy by making each LLM call an easier task.</p>
 *
 */
public class ChainWorkflow {

    private static final String[] DEFAULT_SYSTEM_PROMPTS = {
            // Step 1: Extract numerical values
            """
            Extract only the numerical values and their associated metrics from the text.
            Format each as 'value: metric' on a new line.
            Example format:
            92: customer satisfaction
            45%: revenue growth""",
            // Step 2: Standardize to percentages
            """
            Convert all numerical values to percentages where possible.
            If not a percentage or points, convert to decimal (e.g., 92 points -> 92%).
            Keep one number per line.
            Example format:
            92%: customer satisfaction
            45%: revenue growth""",
            // Step 3: Sort descending
            """
            Sort all lines in descending order by numerical value.
            Keep the format 'value: metric' on each line.
            Example:
            92%: customer satisfaction
            87%: employee satisfaction""",
            // Step 4: Format as markdown table
            """
            Format the sorted data as a markdown table with columns:
            | Metric | Value |
            |:--|--:|
            | Customer Satisfaction | 92% |"""
    };

    private final ChatClient chatClient;
    private final String[] systemPrompts;

    public ChainWorkflow(ChatClient chatClient) {
        this(chatClient, DEFAULT_SYSTEM_PROMPTS);
    }

    public ChainWorkflow(ChatClient chatClient, String[] systemPrompts) {
        this.chatClient = chatClient;
        this.systemPrompts = systemPrompts;
    }

    /**
     * Executes the prompt chaining workflow by processing the input text through
     * a series of LLM calls, where each call's output becomes the input for the next step.
     *
     * @param userInput the input text containing data to be processed
     * @return the final output after all steps have been executed
     */
    public String chain(String userInput) {
        StringBuilder log = new StringBuilder();
        int step = 0;
        String response = userInput;
        log.append(String.format("STEP %d:\n%s\n\n", step++, response));

        for (String prompt : systemPrompts) {
            String input = String.format("{%s}\n {%s}", prompt, response);
            response = chatClient.prompt(input).call().content();
            log.append(String.format("STEP %d:\n%s\n\n", step++, response));
        }

        return log.toString();
    }
}
