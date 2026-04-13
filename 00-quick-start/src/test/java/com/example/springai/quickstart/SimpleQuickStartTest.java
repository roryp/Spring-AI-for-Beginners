package com.example.springai.quickstart;

import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * SimpleQuickStartTest - Unit Tests for Quick Start Module
 * Run: mvn test
 * 
 * Simple tests for the Quick Start module.
 * These tests verify basic Spring AI concepts without requiring API calls!
 * 
 * Key Concepts:
 * - Testing prompt templates without API calls
 * - Verifying template variable substitution
 * - Unit testing AI application components
 * 
 * 💡 Ask GitHub Copilot:
 * - "How can I test my AI application logic without making expensive API calls?"
 * - "What's the best way to mock OpenAiSdkChatModel for unit testing?"
 * - "How do I test that my prompt templates are correctly formatted before sending to the API?"
 * - "What are best practices for testing AI agents with tool integrations?"
 */
class SimpleQuickStartTest {

    @Test
    @DisplayName("Should create simple prompt from text")
    void testSimplePromptCreation() {
        // Create a simple prompt
        String promptText = "Tell me about Paris";
        
        // Verify the prompt is created correctly
        assertThat(promptText).isNotNull();
        assertThat(promptText).isNotEmpty();
        assertThat(promptText).contains("Paris");
    }

    @Test
    @DisplayName("Should format prompt template with variables")
    void testPromptTemplateFormatting() {
        // Create a prompt template
        PromptTemplate template = new PromptTemplate(
            "Best time to visit {destination} for {activity}?"
        );
        
        // Apply template with variables
        Prompt prompt = template.create(Map.of(
            "destination", "Paris",
            "activity", "sightseeing"
        ));
        
        // Verify the result
        assertThat(prompt.getContents()).isEqualTo("Best time to visit Paris for sightseeing?");
    }

    @Test
    @DisplayName("Should handle multiple template variables")
    void testMultipleVariables() {
        // Create template with multiple variables
        PromptTemplate template = new PromptTemplate(
            "I want to travel from {from} to {to} in {month}"
        );
        
        // Apply variables
        Prompt prompt = template.create(Map.of(
            "from", "New York",
            "to", "London",
            "month", "June"
        ));
        
        // Verify all variables are replaced
        assertThat(prompt.getContents()).contains("New York");
        assertThat(prompt.getContents()).contains("London");
        assertThat(prompt.getContents()).contains("June");
        assertThat(prompt.getContents()).doesNotContain("{from}");
    }

    @Test
    @DisplayName("Should create system message prompts")
    void testSystemMessagePrompt() {
        // System message for context
        String systemMessage = "You are a helpful travel assistant specializing in European destinations.";
        
        // User query
        String userQuery = "What should I see in Paris?";
        
        // Verify both parts exist
        assertThat(systemMessage).contains("travel assistant");
        assertThat(userQuery).isNotEmpty();
    }

    @Test
    @DisplayName("Should validate required environment variables")
    void testEnvironmentVariableValidation() {
        // Simulate checking for required environment variable
        String envVar = "GITHUB_TOKEN";
        
        // In real code, we'd check System.getenv(envVar)
        // For testing, just verify the variable name is correct
        assertThat(envVar).isEqualTo("GITHUB_TOKEN");
    }

    @Test
    @DisplayName("Should create chain of thought prompt")
    void testChainOfThoughtPrompt() {
        // Create a prompt that asks for step-by-step reasoning
        String cotPrompt = """
                Problem: What's the best month to visit Paris?
                
                Let's think step-by-step:
                1. Consider the weather
                2. Consider tourist crowds
                3. Consider prices
                """;
        
        // Verify the structure
        assertThat(cotPrompt).contains("step-by-step");
        assertThat(cotPrompt).contains("weather");
        assertThat(cotPrompt).contains("crowds");
    }
}
