package com.example.springai.quickstart;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;

/**
 * SimpleReaderDemo - Simple Document Q&A with Spring AI
 * Run: mvn exec:java -Dexec.mainClass="com.example.springai.quickstart.SimpleReaderDemo"
 *
 * Demonstrates a simple document-grounded chat approach using Spring AI:
 * 1. Load a document from the file system
 * 2. Include the document content as the {@link ChatClient}'s default system message
 * 3. Maintain a conversation history for multi-turn chat
 * 4. Chat with the model — answers are grounded in the document content
 *
 * This is a lightweight "context-stuffing" approach suitable for small documents.
 * For full RAG with vector stores and embeddings, see the 03-rag module.
 *
 * Key Concepts:
 * - Document loading and context injection via {@code defaultSystem(...)}
 * - {@link ChatClient} fluent API for multi-turn conversations
 * - Manual sliding-window history (system + last 20 user/assistant messages)
 *
 * 💡 Ask GitHub Copilot:
 * - "How does this differ from a full RAG pipeline with vector stores?"
 * - "What are the limitations of context-stuffing vs. embedding-based retrieval?"
 * - "How would I scale this to handle larger documents or multiple files?"
 * - "When should I switch from a manual List<Message> to MessageChatMemoryAdvisor?"
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
        var chatOptions = OpenAiChatOptions.builder()
                .baseUrl("https://models.github.ai/inference")
                .apiKey(apiKey)
                .model("gpt-4.1-nano")
                .gitHubModels(true)
                .build();

        OpenAiChatModel chatModel = OpenAiChatModel.builder()
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

        // ChatClient with the document baked in as the default system message.
        // Every turn automatically includes this context without us re-sending it.
        ChatClient chatClient = ChatClient.builder(chatModel)
                .defaultSystem(systemPrompt)
                .build();

        // Maintain conversation history (user/assistant turns only — the system
        // message is supplied by the ChatClient default on every call).
        List<Message> conversationHistory = new ArrayList<>();

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

                // Keep conversation history manageable (last 20 user/assistant messages)
                if (conversationHistory.size() > 20) {
                    List<Message> trimmed = new ArrayList<>(
                            conversationHistory.subList(conversationHistory.size() - 20, conversationHistory.size()));
                    conversationHistory.clear();
                    conversationHistory.addAll(trimmed);
                }

                String answer = chatClient.prompt()
                        .messages(conversationHistory)
                        .call()
                        .content();

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
