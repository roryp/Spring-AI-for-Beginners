package com.example.springai.prompts.service;

import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openaisdk.OpenAiSdkChatModel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Simple tests for Gpt5PromptService demonstrating GPT-5 prompting patterns.
 * These tests validate that prompts are structured correctly according to best practices.
 * 
 * Testing Philosophy for Beginners:
 * - Uses Mockito to mock OpenAiSdkChatModel
 * - ArgumentCaptor captures the actual prompt sent to the model
 * - Tests verify prompt structure contains expected GPT-5 patterns
 * - Doesn't require real LLM - keeps tests fast and deterministic
 * - Validates prompt engineering patterns, not AI responses
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("GPT-5 Prompt Engineering Tests")
class SimpleGpt5PromptTest {

    private Gpt5PromptService promptService;
    
    @Mock
    private OpenAiSdkChatModel mockChatModel;
    
    private ArgumentCaptor<Prompt> promptCaptor;

    private static ChatResponse mockChatResponse(String text) {
        return new ChatResponse(List.of(new Generation(new AssistantMessage(text))));
    }

    @BeforeEach
    void setUp() {
        promptCaptor = ArgumentCaptor.forClass(Prompt.class);
        
        // Default mock behavior
        when(mockChatModel.call(any(Prompt.class)))
            .thenReturn(mockChatResponse("Mocked response"));
        
        promptService = new Gpt5PromptService();
        // Use reflection to inject the mock (since it uses @Autowired)
        setField(promptService, "chatModel", mockChatModel);
    }

    @Test
    @DisplayName("Should generate low eagerness prompt with search depth constraint")
    void shouldGenerateLowEagernessPrompt() {
        // Given
        String problem = "What is 2 + 2?";

        // When
        promptService.solveFocused(problem);

        // Then
        verify(mockChatModel).call(promptCaptor.capture());
        String actualPrompt = getPromptText(promptCaptor.getValue());
        
        assertThat(actualPrompt)
            .contains("<context_gathering>")
            .contains("Search depth: very low")
            .contains("2 reasoning steps")
            .contains(problem);
    }

    @Test
    @DisplayName("Should generate high eagerness prompt for autonomous solving")
    void shouldGenerateHighEagernessPrompt() {
        // Given
        String problem = "Design a scalable microservices architecture";

        // When
        promptService.solveAutonomous(problem);

        // Then
        verify(mockChatModel).call(promptCaptor.capture());
        String actualPrompt = getPromptText(promptCaptor.getValue());
        
        assertThat(actualPrompt)
            .contains("thoroughly")
            .contains("comprehensive solution")
            .contains("multiple approaches")
            .contains(problem);
    }

    @Test
    @DisplayName("Should generate prompt with task execution preambles")
    void shouldGeneratePromptWithPreambles() {
        // Given
        String task = "Create a REST API for user management";

        // When
        promptService.executeWithPreamble(task);

        // Then
        verify(mockChatModel).call(promptCaptor.capture());
        String actualPrompt = getPromptText(promptCaptor.getValue());
        
        assertThat(actualPrompt)
            .contains("<task_execution>")
            .contains("<tool_preambles>")
            .contains("restate the user's goal")
            .contains("step-by-step plan")
            .contains(task);
    }

    @Test
    @DisplayName("Should generate code prompt with self-reflection rubric")
    void shouldGenerateCodeWithReflectionPrompt() {
        // Given
        String requirement = "Create a Spring Boot REST controller for products";

        // When
        promptService.generateCodeWithReflection(requirement);

        // Then
        verify(mockChatModel).call(promptCaptor.capture());
        String actualPrompt = getPromptText(promptCaptor.getValue());
        
        assertThat(actualPrompt)
            .contains("Generate Java code")
            .contains("simple")
            .contains("error handling")
            .contains(requirement);
    }

    @Test
    @DisplayName("Should generate structured analysis prompt")
    void shouldGenerateStructuredAnalysisPrompt() {
        // Given
        String code = "public void process() { System.out.println(\"test\"); }";

        // When
        promptService.analyzeCode(code);

        // Then
        verify(mockChatModel).call(promptCaptor.capture());
        String actualPrompt = getPromptText(promptCaptor.getValue());
        
        assertThat(actualPrompt)
            .contains("<analysis_framework>")
            .contains("<output_format>")
            .contains("Correctness")
            .contains("Best Practices")
            .contains("Performance")
            .contains("Security")
            .contains("Maintainability")
            .contains(code);
    }

    @Test
    @DisplayName("Should maintain conversation context across multiple turns")
    void shouldMaintainConversationContext() {
        // Given
        String sessionId = "test-session";
        when(mockChatModel.call(any(Prompt.class)))
            .thenReturn(mockChatResponse("First response"))
            .thenReturn(mockChatResponse("Second response"));

        // When
        promptService.continueConversation("Hello", sessionId);
        promptService.continueConversation("Tell me more", sessionId);

        // Then
        verify(mockChatModel, times(2)).call(promptCaptor.capture());
        
        Prompt secondCallPrompt = promptCaptor.getAllValues().get(1);
        List<Message> secondCallMessages = secondCallPrompt.getInstructions();
        
        // Should have: System message, first user message, first AI response, second user message
        assertThat(secondCallMessages).hasSizeGreaterThanOrEqualTo(4);
    }

    @Test
    @DisplayName("Should include conversation guidelines in first message")
    void shouldIncludeConversationGuidelinesInFirstMessage() {
        // Given
        String sessionId = "new-session";

        // When
        promptService.continueConversation("First message", sessionId);

        // Then
        verify(mockChatModel).call(promptCaptor.capture());
        List<Message> messages = promptCaptor.getValue().getInstructions();
        
        // Should have system message and user message
        assertThat(messages).hasSizeGreaterThanOrEqualTo(2);
        
        // First message should be system message with guidelines
        Message firstMessage = messages.get(0);
        assertThat(firstMessage).isInstanceOf(SystemMessage.class);
        assertThat(firstMessage.getText())
            .contains("<conversation_guidelines>")
            .contains("<response_style>")
            .contains("<tool_preambles>")
            .contains("Remember previous context");
    }

    @Test
    @DisplayName("Should generate constrained output prompt with strict limits")
    void shouldGenerateConstrainedPrompt() {
        // Given
        String topic = "Java Streams";
        String format = "bullet points";
        int maxWords = 50;

        // When
        promptService.generateConstrained(topic, format, maxWords);

        // Then
        verify(mockChatModel).call(promptCaptor.capture());
        String actualPrompt = getPromptText(promptCaptor.getValue());
        
        assertThat(actualPrompt)
            .contains("<strict_constraints>")
            .contains(topic)
            .contains(format)
            .contains(String.valueOf(maxWords))
            .contains("MUST adhere")
            .contains("Do NOT exceed");
    }

    @Test
    @DisplayName("Should generate step-by-step reasoning prompt")
    void shouldGenerateReasoningPrompt() {
        // Given
        String problem = "Calculate the compound interest";

        // When
        promptService.solveWithReasoning(problem);

        // Then
        verify(mockChatModel).call(promptCaptor.capture());
        String actualPrompt = getPromptText(promptCaptor.getValue());
        
        assertThat(actualPrompt)
            .contains("step by step")
            .contains("understand the problem")
            .contains("approach to solving")
            .contains("Verification")
            .contains(problem);
    }

    @Test
    @DisplayName("Should clear session memory")
    void shouldClearSessionMemory() {
        // Given
        String sessionId = "session-to-clear";
        promptService.continueConversation("Test message", sessionId);

        // When
        promptService.clearSession(sessionId);
        promptService.continueConversation("New message", sessionId);

        // Then - Should start fresh without previous context
        verify(mockChatModel, times(2)).call(promptCaptor.capture());
        
        Prompt newSessionPrompt = promptCaptor.getAllValues().get(1);
        List<Message> newSessionMessages = newSessionPrompt.getInstructions();
        
        // After clearing, should only have system message and new user message (not old messages)
        assertThat(newSessionMessages.size()).isLessThanOrEqualTo(3);
    }

    @Test
    @DisplayName("Should clear all sessions")
    void shouldClearAllSessions() {
        // Given
        promptService.continueConversation("Message 1", "session-1");
        promptService.continueConversation("Message 2", "session-2");

        // When
        promptService.clearAllSessions();
        
        // Then - New conversations should start fresh
        reset(mockChatModel);
        when(mockChatModel.call(any(Prompt.class)))
            .thenReturn(mockChatResponse("Fresh response"));
        
        promptService.continueConversation("New message 1", "session-1");
        
        verify(mockChatModel).call(promptCaptor.capture());
        List<Message> newMessages = promptCaptor.getValue().getInstructions();
        
        // Should start fresh with only system + new message
        assertThat(newMessages.size()).isLessThanOrEqualTo(3);
    }

    @Test
    @DisplayName("Should validate all GPT-5 prompting patterns are present")
    void shouldContainAllGpt5Patterns() {
        // Test that key GPT-5 patterns are used throughout the service
        
        // 1. Low eagerness
        promptService.solveFocused("test");
        verify(mockChatModel, times(1)).call(promptCaptor.capture());
        assertThat(getPromptText(promptCaptor.getValue())).contains("context_gathering");
        
        // 2. Self-reflection (simplified in our implementation)
        reset(mockChatModel);
        when(mockChatModel.call(any(Prompt.class))).thenReturn(mockChatResponse("response"));
        promptService.generateCodeWithReflection("test");
        verify(mockChatModel, times(1)).call(promptCaptor.capture());
        assertThat(getPromptText(promptCaptor.getValue())).contains("production-quality");
        
        // 3. Structured output
        reset(mockChatModel);
        when(mockChatModel.call(any(Prompt.class))).thenReturn(mockChatResponse("response"));
        promptService.analyzeCode("test");
        verify(mockChatModel, times(1)).call(promptCaptor.capture());
        assertThat(getPromptText(promptCaptor.getValue())).contains("analysis_framework");
    }

    /**
     * Extract the text content from a Prompt's first message.
     */
    private String getPromptText(Prompt prompt) {
        List<Message> messages = prompt.getInstructions();
        if (messages.isEmpty()) return "";
        // For single-message prompts, return the first (and only) message text
        // For multi-message prompts (chat), concatenate all message texts
        StringBuilder sb = new StringBuilder();
        for (Message msg : messages) {
            sb.append(msg.getText()).append("\n");
        }
        return sb.toString();
    }

    /**
     * Helper method to set private field using reflection
     * (Simple alternative to @InjectMocks for @Autowired fields)
     */
    private void setField(Object target, String fieldName, Object value) {
        try {
            java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set field " + fieldName, e);
        }
    }
}
