package com.example.springai.rag.app;

import com.example.springai.rag.model.dto.RagRequest;
import com.example.springai.rag.model.dto.RagResponse;
import com.example.springai.rag.model.dto.SourceReference;
import com.example.springai.rag.service.RagService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for RagController.
 * Uses Jackson 3 (tools.jackson) which is the default JSON library in Spring Boot 4.0.0.
 */
@WebMvcTest(RagController.class)
class RagControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper jsonMapper;

    @MockitoBean
    private RagService ragService;

    @Test
    void testAskWithValidQuestion() throws Exception {
        // Given
        RagRequest request = new RagRequest("What is Azure OpenAI?", "conv-123", 5);
        
        List<SourceReference> sources = new ArrayList<>();
        sources.add(new SourceReference("doc.pdf", "Azure OpenAI provides...", 0.95));
        
        RagResponse response = new RagResponse(
            "Azure OpenAI is a cloud service...",
            "conv-123",
            sources
        );
        
        when(ragService.ask(any(RagRequest.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/rag/ask")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.answer").value(response.answer()))
                .andExpect(jsonPath("$.conversationId").value("conv-123"))
                .andExpect(jsonPath("$.sources").isArray())
                .andExpect(jsonPath("$.sources[0].filename").value("doc.pdf"));
    }

    @Test
    void testAskWithEmptyQuestion() throws Exception {
        // Given - Using JSON directly to bypass record validation
        String requestJson = "{\"question\":\"\",\"conversationId\":\"conv-123\",\"maxResults\":5}";

        // When & Then - The controller should return 400 with no body (Spring validation error)
        mockMvc.perform(post("/api/rag/ask")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testAskWithNullQuestion() throws Exception {
        // Given - Using JSON directly to bypass validation
        String requestJson = "{\"question\":null,\"conversationId\":\"conv-123\",\"maxResults\":5}";

        // When & Then
        mockMvc.perform(post("/api/rag/ask")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testAskWithServiceException() throws Exception {
        // Given
        RagRequest request = new RagRequest("What is Azure OpenAI?", "conv-123", 5);
        when(ragService.ask(any(RagRequest.class)))
            .thenThrow(new RuntimeException("Service error"));

        // When & Then
        mockMvc.perform(post("/api/rag/ask")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void testHealthEndpoint() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/rag/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("RAG service is healthy"));
    }
}
