package com.example.springai.quickstart;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openaisdk.OpenAiSdkChatModel;
import org.springframework.ai.openaisdk.OpenAiSdkChatOptions;

/**
 * SimpleReaderDemo - Simple Document Q&A with Spring AI
 * Run: mvn exec:java -Dexec.mainClass="com.example.springai.quickstart.SimpleReaderDemo"
 *
 * Demonstrates a simple document-grounded chat approach using Spring AI:
 * 1. Load a document from the file system
 * 2. Include the document content as context in the system message
 * 3. Maintain a conversation history for multi-turn chat
 * 4. Chat with the model — answers are grounded in the document content
 *
 * This is a lightweight "context-stuffing" approach suitable for small documents.
 * For full RAG with vector stores and embeddings, see the 03-rag module.
 *
 * Key Concepts:
 * - Document loading and context injection
 * - System message for grounding the model
 * - Multi-turn conversation with message history
 *
 * 💡 Ask GitHub Copilot:
 * - "How does this differ from a full RAG pipeline with vector stores?"
 * - "What are the limitations of context-stuffing vs. embedding-based retrieval?"
 * - "How would I scale this to handle larger documents or multiple files?"
 */
public class SimpleReaderDemo {

    public static void main(String[] args) throws IOException {
        // --- Validate environment ---
        String apiKey = System.getenv("GITHUB_TOKEN");
        if (apiKey == null || apiKey.trim().isEmpty()) {
            System.err.println("Error: GITHUB_TOKEN environment variable is not set or is empty.");
            System.exit(1);
        }

        // --- 1. Load the sample document ---
        Path documentPath = resolveDocumentPath();
        System.out.println("Loading document: " + documentPath);
        String documentContent = Files.readString(documentPath);
        System.out.println("Loaded document (" + documentContent.length() + " characters).");

        // --- 2. Create the chat model (GitHub Models / OpenAI-compatible) ---
        var chatOptions = OpenAiSdkChatOptions.builder()
                .baseUrl("https://models.github.ai/inference")
                .apiKey(apiKey)
                .model("gpt-4.1-nano")
                .gitHubModels(true)
                .build();

        var chatModel = OpenAiSdkChatModel.builder()
                .options(chatOptions)
                .build();

        // --- 3. Build the system message with document context ---
        String systemPrompt = """
                You are a helpful assistant that answers questions based on the provided document.
                Use the document content below to answer user questions accurately.
                If the answer is not in the document, say so clearly.
                
                --- DOCUMENT ---
                %s
                --- END DOCUMENT ---
                """.formatted(documentContent);

        SystemMessage systemMessage = new SystemMessage(systemPrompt);

        // Maintain conversation history (up to 10 recent messages)
        List<Message> conversationHistory = new ArrayList<>();
        conversationHistory.add(systemMessage);

        // --- 4. Interactive conversation loop ---
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.println("\nAsk questions about the loaded document (type 'exit' to quit):\n");
            while (true) {
                System.out.print("You: ");
                String question = scanner.nextLine();
                if (question == null || question.trim().equalsIgnoreCase("exit")) {
                    break;
                }

                conversationHistory.add(new UserMessage(question));

                // Keep conversation history manageable (system + last 10 user/assistant messages)
                if (conversationHistory.size() > 21) {
                    List<Message> trimmed = new ArrayList<>();
                    trimmed.add(systemMessage);
                    trimmed.addAll(conversationHistory.subList(conversationHistory.size() - 20, conversationHistory.size()));
                    conversationHistory = trimmed;
                }

                ChatResponse chatResponse = chatModel.call(new Prompt(conversationHistory));
                String answer = chatResponse.getResult().getOutput().getText();

                conversationHistory.add(new AssistantMessage(answer));

                System.out.println("\nAssistant: " + answer + "\n");
            }
        }
    }

    /** Resolve the path to the sample document.txt file. */
    private static Path resolveDocumentPath() {
        for (String candidate : new String[]{"00-quick-start", "."}) {
            Path doc = Paths.get(candidate, "document.txt");
            if (doc.toFile().isFile()) {
                return doc;
            }
        }
        return Paths.get("document.txt");
    }
}
