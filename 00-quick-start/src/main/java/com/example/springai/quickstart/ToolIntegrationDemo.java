package com.example.springai.quickstart;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.function.FunctionToolCallback;

/**
 * ToolIntegrationDemo - AI Function Calling with Spring AI
 * Run: mvn exec:java -Dexec.mainClass="com.example.springai.quickstart.ToolIntegrationDemo"
 *
 * This example demonstrates one of the most powerful features of modern LLMs:
 * the ability to call external functions/tools. The AI can:
 * 1. Understand when a tool is needed
 * 2. Extract parameters from natural language
 * 3. Execute the tool and incorporate results
 *
 * This enables AI agents to perform actions, not just generate text.
 * We'll use a calculator as an example, but this pattern applies to
 * any functionality: databases, APIs, file operations, etc.
 *
 * Key Concepts:
 * - {@link FunctionToolCallback} for registering tools
 * - ChatClient.Builder.defaultTools(...) to attach tools once
 * - Parameter extraction from natural language
 * - Multi-step reasoning with tools
 *
 * 💡 Ask GitHub Copilot:
 * - "How does FunctionToolCallback work and what does Spring AI do with it behind the scenes?"
 * - "Can the AI call multiple tools in sequence to solve complex problems?"
 * - "What happens if a tool throws an exception - how should I handle errors?"
 * - "How would I integrate a real API (like weather or currency) instead of this calculator example?"
 * - "When should I use ChatClient.prompt().tools(...) per-call vs defaultTools(...) at build time?"
 */
public class ToolIntegrationDemo {

    public static void main(String[] args) {
        // Ensure authentication token is present
        String githubToken = System.getenv("GITHUB_TOKEN");
        if (githubToken == null || githubToken.isEmpty()) {
            System.err.println("ERROR: Missing required GITHUB_TOKEN environment variable!");
            System.err.println("   Set using: export GITHUB_TOKEN=\"your_token_here\"");
            System.exit(1);
        }

        showHeader("AI Tool Integration Demo");

        // Register calculator tools as FunctionToolCallbacks
        List<ToolCallback> toolCallbacks = List.of(
            FunctionToolCallback.builder("add", ADD)
                .description("Performs addition of two numeric values")
                .inputType(TwoNumbers.class)
                .build(),
            FunctionToolCallback.builder("subtract", SUBTRACT)
                .description("Performs subtraction: first value minus second value")
                .inputType(TwoNumbers.class)
                .build(),
            FunctionToolCallback.builder("multiply", MULTIPLY)
                .description("Performs multiplication of two numeric values")
                .inputType(TwoNumbers.class)
                .build(),
            FunctionToolCallback.builder("divide", DIVIDE)
                .description("Performs division: first value divided by second value")
                .inputType(TwoNumbers.class)
                .build(),
            FunctionToolCallback.builder("squareRoot", SQUARE_ROOT)
                .description("Computes the square root of a number")
                .inputType(SingleNumber.class)
                .build()
        );

        // Bootstrap the chat model (no tools at the model level any more — tools are
        // now an application-layer concern attached via the ChatClient builder).
        var chatOptions = OpenAiChatOptions.builder()
                .baseUrl("https://models.github.ai/inference")
                .apiKey(githubToken)
                .model("gpt-4.1-nano")
                .gitHubModels(true)
                .build();

        OpenAiChatModel chatModel = OpenAiChatModel.builder()
                .options(chatOptions)
                .build();

        // Wire the tool callbacks onto the ChatClient once via the ToolSpec DSL.
        // Spring AI handles the tool-call loop (model -> tool -> model) automatically.
        ChatClient chatClient = ChatClient.builder(chatModel)
                .defaultTools(t -> t.callbacks(toolCallbacks))
                .build();

        // Maintain conversation history for multi-turn context
        List<Message> conversationHistory = new ArrayList<>();

        // Scenario 1: Basic operation
        System.out.println("\nScenario 1: Simple Arithmetic");
        System.out.println("-".repeat(60));
        String query1 = "Can you calculate 42 plus 58 for me?";
        System.out.println("User: " + query1);
        System.out.println();
        String result1 = chat(chatClient, conversationHistory, query1);
        System.out.println("Assistant: " + result1);
        System.out.println();

        // Scenario 2: Multi-step reasoning
        System.out.println("Scenario 2: Complex Calculation");
        System.out.println("-".repeat(60));
        String query2 = "I purchased an item for 80 dollars with a 15% discount. What's the final price?";
        System.out.println("User: " + query2);
        System.out.println();
        String result2 = chat(chatClient, conversationHistory, query2);
        System.out.println("Assistant: " + result2);
        System.out.println();

        // Scenario 3: Advanced function
        System.out.println("Scenario 3: Mathematical Function");
        System.out.println("-".repeat(60));
        String query3 = "Calculate the square root of 256 please";
        System.out.println("User: " + query3);
        System.out.println();
        String result3 = chat(chatClient, conversationHistory, query3);
        System.out.println("Assistant: " + result3);
        System.out.println();

        System.out.println("[SUCCESS] All tool integration scenarios completed!");
        System.out.println();
        System.out.println("Key Observations:");
        System.out.println("   - AI autonomously selected appropriate tools");
        System.out.println("   - Parameters extracted from conversational language");
        System.out.println("   - Multiple tool calls coordinated automatically");
    }

    // Input record for two-number operations
    record TwoNumbers(double a, double b) {}

    // Input record for single-number operations
    record SingleNumber(double number) {}

    // Calculator functions
    static final Function<TwoNumbers, Double> ADD = input -> {
        System.out.println("[TOOL] Executing: add(" + input.a() + ", " + input.b() + ")");
        return input.a() + input.b();
    };

    static final Function<TwoNumbers, Double> SUBTRACT = input -> {
        System.out.println("[TOOL] Executing: subtract(" + input.a() + ", " + input.b() + ")");
        return input.a() - input.b();
    };

    static final Function<TwoNumbers, Double> MULTIPLY = input -> {
        System.out.println("[TOOL] Executing: multiply(" + input.a() + ", " + input.b() + ")");
        return input.a() * input.b();
    };

    static final Function<TwoNumbers, Double> DIVIDE = input -> {
        System.out.println("[TOOL] Executing: divide(" + input.a() + ", " + input.b() + ")");
        if (input.b() == 0) {
            throw new IllegalArgumentException("Division by zero is undefined");
        }
        return input.a() / input.b();
    };

    static final Function<SingleNumber, Double> SQUARE_ROOT = input -> {
        System.out.println("[TOOL] Executing: squareRoot(" + input.number() + ")");
        return Math.sqrt(input.number());
    };

    /**
     * Sends a message to the chat client, maintaining conversation history.
     * The {@link ChatClient} fluent API drives the tool-call loop automatically:
     * the model decides when to call a registered tool, Spring AI executes it,
     * feeds the result back, and returns only the final assistant text.
     */
    private static String chat(ChatClient chatClient, List<Message> history, String userMessage) {
        history.add(new UserMessage(userMessage));

        // Keep history manageable (last 20 messages = ~10 user/assistant pairs)
        if (history.size() > 20) {
            List<Message> trimmed = new ArrayList<>(history.subList(history.size() - 20, history.size()));
            history.clear();
            history.addAll(trimmed);
        }

        String text = chatClient.prompt()
                .messages(history)
                .call()
                .content();

        history.add(new AssistantMessage(text));
        return text;
    }

    private static void showHeader(String title) {
        System.out.println("=".repeat(60));
        System.out.println("  " + title);
        System.out.println("=".repeat(60));
    }
}
