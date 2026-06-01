package com.example.springai.rag.service;

import com.example.springai.rag.config.RagSearchProperties;
import com.example.springai.rag.model.dto.RagRequest;
import com.example.springai.rag.model.dto.RagResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdvisorRagServiceTest {

    @Mock
    private ChatClient chatClient;

    @Mock
    private ChatClient.ChatClientRequestSpec requestSpec;

    @Mock
    private ChatClient.CallResponseSpec callResponseSpec;

    @Mock
    private VectorStore vectorStore;

    private AdvisorRagService advisorRagService;

    @BeforeEach
    void setUp() {
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.advisors(any(Advisor.class))).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.chatResponse())
                .thenReturn(new ChatResponse(List.of(new Generation(new AssistantMessage("Grounded answer")))));
        when(vectorStore.similaritySearch(any(SearchRequest.class))).thenReturn(List.of());

        advisorRagService = new AdvisorRagService(chatClient, vectorStore, new RagSearchProperties(5, 0.5));
    }

    @Test
    void advisorSourceSearchUsesNonzeroSimilarityThreshold() {
        RagResponse response = advisorRagService.ask(new RagRequest("What is Spring AI?", "conv-2", 5));

        ArgumentCaptor<SearchRequest> searchRequest = ArgumentCaptor.forClass(SearchRequest.class);
        verify(vectorStore).similaritySearch(searchRequest.capture());
        assertThat(searchRequest.getValue().getTopK()).isEqualTo(5);
        assertThat(searchRequest.getValue().getSimilarityThreshold()).isEqualTo(0.5);
        assertThat(searchRequest.getValue().getQuery()).isEqualTo("What is Spring AI?");
        assertThat(response.answer()).isEqualTo("Grounded answer");
        assertThat(response.sources()).isEmpty();
    }
}