package com.example.springai.agents.patterns;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.springframework.ai.chat.client.ChatClient;

/**
 * Implements the Parallelization Workflow pattern for efficient concurrent processing
 * of multiple LLM operations. The pattern manifests in two key variations:
 *
 * <ul>
 * <li><b>Sectioning</b>: Decomposes a task into independent subtasks processed concurrently.</li>
 * <li><b>Voting</b>: Executes identical prompts multiple times in parallel for diverse perspectives.</li>
 * </ul>
 *
 */
public class ParallelizationWorkflow {

    private final ChatClient chatClient;

    public ParallelizationWorkflow(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    /**
     * Processes multiple inputs concurrently using a fixed thread pool and the same prompt template.
     * Maintains the order of results corresponding to the input order.
     *
     * @param prompt   the prompt template to use for each input
     * @param inputs   list of input strings to process in parallel
     * @param nWorkers the number of concurrent worker threads
     * @return list of processed results in the same order as the inputs
     */
    public List<String> parallel(String prompt, List<String> inputs, int nWorkers) {
        ExecutorService executor = Executors.newFixedThreadPool(nWorkers);
        try {
            List<CompletableFuture<String>> futures = inputs.stream()
                    .map(input -> CompletableFuture.supplyAsync(() ->
                            chatClient.prompt(prompt + "\nInput: " + input).call().content(), executor))
                    .collect(Collectors.toList());

            CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).join();

            return futures.stream()
                    .map(CompletableFuture::join)
                    .collect(Collectors.toList());
        } finally {
            executor.shutdown();
        }
    }
}
