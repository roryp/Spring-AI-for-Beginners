package com.example.springai.quickstart;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;

/**
 * BasicChatDemo - Introduction to Spring AI Chat Capabilities
 * Run: mvn exec:java -Dexec.mainClass="com.example.springai.quickstart.BasicChatDemo"
 *
 * This example shows how to set up and use a language model for basic chat interactions.
 * We're using GitHub Models which provides an OpenAI-compatible interface, making it
 * easy to switch between different providers later if needed.
 *
 * Key Concepts:
 * - Model initialization with custom endpoints
 * - {@link ChatClient} fluent API for prompt -> response in a single line
 * - Environment-based authentication
 *
 * Why ChatClient? It's Spring AI's recommended high-level entry point: a fluent
 * builder that produces a {@code String} answer with one call, supports advisors,
 * structured output, tool calling, and streaming. The underlying
 * {@link OpenAiChatModel} is still built explicitly here because this demo is a
 * plain {@code main()} method outside a Spring context — in a Spring Boot app the
 * starter would auto-configure both the model and a {@code ChatClient.Builder}.
 *
 * 💡 Ask GitHub Copilot:
 * - "How would I switch from GitHub Models to Microsoft Foundry in this code?"
 * - "What other parameters can I configure in OpenAiChatOptions.builder()?"
 * - "How do I add streaming responses using ChatClient.prompt(...).stream()?"
 * - "When would I drop ChatClient and call OpenAiChatModel.call(new Prompt(...)) directly?"
 */
public class BasicChatDemo {

    public static void main(String[] args) {
        // Validate that we have the necessary GitHub authentication token
        String githubToken = System.getenv("GITHUB_TOKEN");
        if (githubToken == null || githubToken.isEmpty()) {
            System.err.println("ERROR: Missing GITHUB_TOKEN environment variable!");
            System.err.println("   Set it using: export GITHUB_TOKEN=\"your_token_here\"");
            System.exit(1);
        }

        printHeader("Basic Chat Demo");

        // Bootstrap the chat model for GitHub Models endpoint (gpt-4.1-nano).
        // OpenAiChatOptions here is one-time wiring — model name, endpoint, and the
        // gitHubModels flag — not per-request configuration.
        var chatOptions = OpenAiChatOptions.builder()
                .baseUrl("https://models.github.ai/inference")
                .apiKey(githubToken)
                .model("gpt-4.1-nano")
                .gitHubModels(true)
                .build();

        var chatModel = OpenAiChatModel.builder()
                .options(chatOptions)
                .build();

        // Wrap the model in a ChatClient — the recommended Spring AI fluent API.
        var chatClient = ChatClient.create(chatModel);

        // Prepare our query
        String question = "What are the main benefits of using Spring AI in enterprise applications?";
        System.out.println("User Query: " + question);
        System.out.println();

        // Execute the chat request and get response text in one fluent call
        String response = chatClient.prompt(question).call().content();

        // Display the model's response
        System.out.println("Assistant Response:");
        System.out.println(response);
        System.out.println();
        System.out.println("[SUCCESS] Chat interaction completed successfully!");
    }

    private static void printHeader(String title) {
        String border = "=".repeat(60);
        System.out.println(border);
        System.out.println("  " + title);
        System.out.println(border);
        System.out.println();
    }
}
