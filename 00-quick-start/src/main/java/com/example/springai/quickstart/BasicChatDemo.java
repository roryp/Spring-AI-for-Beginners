package com.example.springai.quickstart;

import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openaisdk.OpenAiSdkChatModel;
import org.springframework.ai.openaisdk.OpenAiSdkChatOptions;

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
 * - Synchronous chat completion
 * - Environment-based authentication
 * 
 * 💡 Ask GitHub Copilot:
 * - "How would I switch from GitHub Models to Azure OpenAI in this code?"
 * - "What other parameters can I configure in OpenAiSdkChatOptions.builder()?"
 * - "How do I add streaming responses instead of waiting for the complete response?"
 * - "What builder methods are available for configuring the OpenAI SDK client?"
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

        // Configure the chat model options for GitHub Models endpoint
        // This uses GPT-4.1-nano through GitHub's inference API
        var chatOptions = OpenAiSdkChatOptions.builder()
                .baseUrl("https://models.github.ai/inference")
                .apiKey(githubToken)
                .model("gpt-4.1-nano")
                .gitHubModels(true)
                .build();

        // Build the Spring AI chat model
        var chatModel = OpenAiSdkChatModel.builder()
                .options(chatOptions)
                .build();

        // Prepare our query
        String question = "What are the main benefits of using Spring AI in enterprise applications?";
        System.out.println("User Query: " + question);
        System.out.println();

        // Execute the chat request and get response
        ChatResponse chatResponse = chatModel.call(new Prompt(question));
        String response = chatResponse.getResult().getOutput().getText();

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
