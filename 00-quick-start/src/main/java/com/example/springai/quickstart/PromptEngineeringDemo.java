package com.example.springai.quickstart;

import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.openaisdk.OpenAiSdkChatModel;
import org.springframework.ai.openaisdk.OpenAiSdkChatOptions;

import java.util.Map;

/**
 * PromptEngineeringDemo - Basic Prompt Engineering Patterns
 * Run: mvn exec:java -Dexec.mainClass="com.example.springai.quickstart.PromptEngineeringDemo"
 * 
 * This demonstrates fundamental prompt engineering techniques that improve AI responses:
 * 
 * 1. Zero-shot prompting - Direct task instruction without examples
 * 2. Few-shot prompting - Providing examples to guide the model
 * 3. Chain of thought - Asking the model to show its reasoning
 * 4. Role-based prompting - Setting context and persona
 * 
 * Uses GitHub Models (gpt-4.1-nano) which works better with rate limits
 * than larger models for simple demonstrations.
 * 
 * Key Concepts:
 * - Different prompting strategies
 * - Temperature parameter for response variability
 * - Prompt structure and formatting
 * 
 * 💡 Ask GitHub Copilot:
 * - "What's the difference between zero-shot and few-shot prompting, and when should I use each?"
 * - "How does the temperature parameter affect the model's responses?"
 * - "What are some techniques to prevent prompt injection attacks in production?"
 * - "How can I create reusable PromptTemplate objects for common patterns?"
 */
public class PromptEngineeringDemo {

    public static void main(String[] args) {
        // Verify GitHub token is available
        String githubToken = System.getenv("GITHUB_TOKEN");
        if (githubToken == null || githubToken.isEmpty()) {
            System.err.println("ERROR: GITHUB_TOKEN not found in environment!");
            System.err.println("   Configure with: export GITHUB_TOKEN=\"your_token_here\"");
            System.exit(1);
        }

        displayHeader("Prompt Engineering Patterns Demo");

        // Configure the chat model options for GitHub Models endpoint
        var chatOptions = OpenAiSdkChatOptions.builder()
                .baseUrl("https://models.github.ai/inference")
                .apiKey(githubToken)
                .model("gpt-4.1-nano")
                .temperature(0.7)
                .gitHubModels(true)
                .build();

        // Build the Spring AI chat model
        var chatModel = OpenAiSdkChatModel.builder()
                .options(chatOptions)
                .build();

        // Pattern 1: Zero-shot - Direct instruction
        demonstrateZeroShot(chatModel);
        
        // Pattern 2: Few-shot - Learning from examples
        demonstrateFewShot(chatModel);
        
        // Pattern 3: Chain of Thought - Show reasoning
        demonstrateChainOfThought(chatModel);
        
        // Pattern 4: Role-based - Setting context
        demonstrateRoleBased(chatModel);

        // Pattern 5: Prompt Templates - Reusable prompts with variables
        demonstratePromptTemplates(chatModel);

        // Pattern 6: Conversational Memory - Maintaining context across turns
        demonstrateConversationalMemory(chatModel);
    }

    /**
     * Pattern 1: Zero-shot Prompting
     * Direct task instruction without examples
     */
    private static void demonstrateZeroShot(OpenAiSdkChatModel chatModel) {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("PATTERN 1: Zero-Shot Prompting");
        System.out.println("=".repeat(60));
        System.out.println("Simple, direct instructions without examples");
        System.out.println();

        String prompt = "Classify this sentiment: 'I absolutely loved the movie!'";
        System.out.println("Prompt: " + prompt);
        System.out.println();
        
        ChatResponse chatResponse = chatModel.call(new Prompt(prompt));
        System.out.println("Response: " + chatResponse.getResult().getOutput().getText());
    }

    /**
     * Pattern 2: Few-Shot Prompting
     * Provide examples to guide the model's behavior
     */
    private static void demonstrateFewShot(OpenAiSdkChatModel chatModel) {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("PATTERN 2: Few-Shot Prompting");
        System.out.println("=".repeat(60));
        System.out.println("Learning from examples to understand the pattern");
        System.out.println();

        String prompt = """
                Classify the sentiment as positive, negative, or neutral.
                
                Examples:
                Text: "This product exceeded my expectations!" → Positive
                Text: "It's okay, nothing special." → Neutral
                Text: "Waste of money, very disappointed." → Negative
                
                Now classify this:
                Text: "Best purchase I've made all year!"
                """;
        
        System.out.println("Prompt with examples:");
        System.out.println(prompt);
        
        ChatResponse chatResponse = chatModel.call(new Prompt(prompt));
        System.out.println("Response: " + chatResponse.getResult().getOutput().getText());
    }

    /**
     * Pattern 3: Chain of Thought
     * Ask the model to explain its reasoning step-by-step
     */
    private static void demonstrateChainOfThought(OpenAiSdkChatModel chatModel) {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("PATTERN 3: Chain of Thought");
        System.out.println("=".repeat(60));
        System.out.println("Asking the model to show its reasoning process");
        System.out.println();

        String prompt = """
                Problem: A store has 15 apples. They sell 8 apples and then 
                receive a shipment of 12 more apples. How many apples do they have now?
                
                Let's solve this step-by-step:
                """;
        
        System.out.println("Prompt: " + prompt);
        
        ChatResponse chatResponse = chatModel.call(new Prompt(prompt));
        System.out.println("Response: " + chatResponse.getResult().getOutput().getText());
    }

    /**
     * Pattern 4: Role-Based Prompting
     * Set a specific persona/context for the AI
     */
    private static void demonstrateRoleBased(OpenAiSdkChatModel chatModel) {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("PATTERN 4: Role-Based Prompting");
        System.out.println("=".repeat(60));
        System.out.println("Setting context and persona for specialized responses");
        System.out.println();

        String prompt = """
                You are an experienced software architect reviewing code.
                Provide a brief code review for this function:
                
                def calculate_total(items):
                    total = 0
                    for item in items:
                        total = total + item['price']
                    return total
                """;
        
        System.out.println("Prompt with role:");
        System.out.println(prompt);
        
        ChatResponse chatResponse = chatModel.call(new Prompt(prompt));
        System.out.println("Response: " + chatResponse.getResult().getOutput().getText());
    }

    /**
     * Pattern 5: Prompt Templates
     * Create reusable prompts with variable placeholders
     */
    private static void demonstratePromptTemplates(OpenAiSdkChatModel chatModel) {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("PATTERN 5: Prompt Templates");
        System.out.println("=".repeat(60));
        System.out.println("Reusable prompts with variable placeholders");
        System.out.println();

        PromptTemplate template = new PromptTemplate(
            "What's the best time to visit {destination} for {activity}?"
        );

        Prompt prompt = template.create(Map.of(
            "destination", "Paris",
            "activity", "sightseeing"
        ));

        System.out.println("Template: What's the best time to visit {destination} for {activity}?");
        System.out.println("Variables: destination=Paris, activity=sightseeing");
        System.out.println("Resolved prompt: " + prompt.getContents());
        System.out.println();

        ChatResponse chatResponse = chatModel.call(prompt);
        System.out.println("Response: " + chatResponse.getResult().getOutput().getText());

        // Second example with different variables
        System.out.println();
        Prompt prompt2 = template.create(Map.of(
            "destination", "Tokyo",
            "activity", "cherry blossom viewing"
        ));

        System.out.println("Same template, different variables: destination=Tokyo, activity=cherry blossom viewing");
        System.out.println("Resolved prompt: " + prompt2.getContents());
        System.out.println();

        ChatResponse chatResponse2 = chatModel.call(prompt2);
        System.out.println("Response: " + chatResponse2.getResult().getOutput().getText());
    }

    /**
     * Pattern 6: Conversational Memory
     * Maintain context across multiple interactions using ChatMemory.
     * This is the only pattern that requires memory — the follow-up question
     * references information from the first turn.
     */
    private static void demonstrateConversationalMemory(OpenAiSdkChatModel chatModel) {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("PATTERN 6: Conversational Memory");
        System.out.println("=".repeat(60));
        System.out.println("Maintaining context across multiple interactions");
        System.out.println();

        // Create a memory window that keeps the last 10 messages
        ChatMemory chatMemory = MessageWindowChatMemory.builder()
                .maxMessages(10)
                .build();
        String conversationId = "demo-session";

        // First interaction
        String question1 = "My name is Alex and I'm learning Spring AI. What are the key concepts I should focus on?";
        System.out.println("User (Turn 1): " + question1);
        System.out.println();

        UserMessage userMessage1 = new UserMessage(question1);
        chatMemory.add(conversationId, userMessage1);
        ChatResponse response1 = chatModel.call(new Prompt(chatMemory.get(conversationId)));
        chatMemory.add(conversationId, response1.getResult().getOutput());

        System.out.println("Assistant: " + response1.getResult().getOutput().getText());
        System.out.println();

        // Second interaction — references the first turn
        String question2 = "What's my name, and can you elaborate on the first concept you mentioned?";
        System.out.println("User (Turn 2): " + question2);
        System.out.println();

        UserMessage userMessage2 = new UserMessage(question2);
        chatMemory.add(conversationId, userMessage2);
        ChatResponse response2 = chatModel.call(new Prompt(chatMemory.get(conversationId)));
        chatMemory.add(conversationId, response2.getResult().getOutput());

        System.out.println("Assistant: " + response2.getResult().getOutput().getText());
        System.out.println();
        System.out.println("[INFO] The model remembered your name and previous context using ChatMemory!");
    }

    private static void displayHeader(String title) {
        System.out.println("=".repeat(60));
        System.out.println("  " + title);
        System.out.println("=".repeat(60));
        System.out.println();
    }
}
