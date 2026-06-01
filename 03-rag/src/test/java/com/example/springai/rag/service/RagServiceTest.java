package com.example.springai.rag.service;

import com.example.springai.rag.config.RagSearchProperties;
import com.example.springai.rag.model.dto.RagRequest;
import com.example.springai.rag.model.dto.RagResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RagServiceTest {

    @Mock
    private OpenAiChatModel chatModel;

    @Mock
    private VectorStore vectorStore;

    @Test
    void nativeRagSearchUsesNonzeroSimilarityThreshold() {
        RagSearchProperties searchProperties = new RagSearchProperties(5, 0.5);
        RagService ragService = new RagService(chatModel, vectorStore, searchProperties);
        when(vectorStore.similaritySearch(any(SearchRequest.class))).thenReturn(List.of());

        RagResponse response = ragService.ask(new RagRequest("What is unrelated?", "conv-1", 5));

        ArgumentCaptor<SearchRequest> searchRequest = ArgumentCaptor.forClass(SearchRequest.class);
        verify(vectorStore).similaritySearch(searchRequest.capture());
        assertThat(searchRequest.getValue().getTopK()).isEqualTo(5);
        assertThat(searchRequest.getValue().getSimilarityThreshold()).isEqualTo(0.5);
        assertThat(searchRequest.getValue().getQuery()).isEqualTo("What is unrelated?");
        assertThat(response.sources()).isEmpty();
        assertThat(response.answer()).contains("cannot answer");
        verifyNoInteractions(chatModel);
    }
}