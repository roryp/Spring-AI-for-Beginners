package com.example.springai.agents.patterns;

import java.util.Map;

import org.springframework.ai.chat.client.AdvisorParams;
import org.springframework.ai.chat.client.ChatClient;

/**
 * Implements the Routing workflow pattern that classifies input and directs it
 * to specialized followup tasks. Uses an LLM to analyze input content and route
 * it to the most appropriate specialized prompt or handler.
 *
 * <p>When to use: Complex tasks with distinct categories of input that require
 * different handling or specialized processing.</p>
 *
 * 💡 Ask GitHub Copilot:
 * - "Why is a small routing LLM better than stuffing all cases into one big prompt?"
 * - "How do I improve routing accuracy when categories overlap or the input is ambiguous?"
 * - "How could I add a fallback route for inputs that don't match any category confidently?"
 * - "Can I replace the LLM router with a cheaper classifier (embeddings or rules) for known cases?"
 */
public class RoutingWorkflow {

    public record RoutingResponse(String reasoning, String selection) {
    }

    private final ChatClient chatClient;

    public RoutingWorkflow(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    /**
     * Routes input to a specialized prompt based on content classification.
     *
     * @param input  the input text to be routed and processed
     * @param routes map of route names to their corresponding specialized prompts
     * @return processed response from the selected specialized route
     */
    @SuppressWarnings("null")
    public String route(String input, Map<String, String> routes) {
        String routeKey = determineRoute(input, routes.keySet());

        String selectedPrompt = routes.get(routeKey);
        if (selectedPrompt == null) {
            throw new IllegalArgumentException("Selected route '" + routeKey + "' not found in routes map");
        }

        String specializedResponse = chatClient.prompt()
                .user(u -> u.text("{routePrompt}\nInput: {input}")
                        .param("routePrompt", selectedPrompt)
                        .param("input", input))
                .call()
                .content();

        return "Route selected: **" + routeKey + "**\n\n" + specializedResponse;
    }

    @SuppressWarnings("null")
    private String determineRoute(String input, Iterable<String> availableRoutes) {
        String selectorPrompt = """
                Analyze the input and select the most appropriate support team from these options: {routes}
                First explain your reasoning, then provide your selection in this JSON format:

                \\{
                    "reasoning": "Brief explanation of why this ticket should be routed to a specific team.
                                  Consider key terms, user intent, and urgency level.",
                    "selection": "The chosen team name"
                \\}

                Input: {input}""";

        RoutingResponse routingResponse = chatClient.prompt()
                .user(u -> u.text(selectorPrompt)
                        .param("routes", availableRoutes)
                        .param("input", input))
                .advisors(AdvisorParams.ENABLE_NATIVE_STRUCTURED_OUTPUT)
                .call()
                .entity(RoutingResponse.class);
        return routingResponse.selection();
    }
}
