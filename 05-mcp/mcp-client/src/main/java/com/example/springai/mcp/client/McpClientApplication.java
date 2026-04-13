package com.example.springai.mcp.client;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class McpClientApplication {

	public static void main(String[] args) {
		SpringApplication.run(McpClientApplication.class, args).close();
	}

	@Bean
	public CommandLineRunner predefinedQuestions(ChatClient.Builder chatClientBuilder,
			ToolCallbackProvider toolCallbackProvider) {

		return args -> {

			ChatClient chatClient = chatClientBuilder.defaultToolCallbacks(toolCallbackProvider).build();

			String userQuestion = "What is the weather in Amsterdam right now?";

			System.out.println("> USER: " + userQuestion);
			System.out.println("> ASSISTANT: " + chatClient.prompt(userQuestion).call().content());
		};
	}
}
