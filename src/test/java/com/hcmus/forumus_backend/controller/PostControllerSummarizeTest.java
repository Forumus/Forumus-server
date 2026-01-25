package com.hcmus.forumus_backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hcmus.forumus_backend.dto.post.PostSummaryRequest;
import com.hcmus.forumus_backend.dto.post.PostSummaryResponse;
import com.hcmus.forumus_backend.service.PostService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for PostController summarize endpoint.
 * Tests various input scenarios and response handling.
 */
@ExtendWith(MockitoExtension.class)
class PostControllerSummarizeTest {

    private MockMvc mockMvc;

    @Mock
    private PostService postService;

    @Mock
    private com.hcmus.forumus_backend.service.NotificationService notificationService;

    @InjectMocks
    private PostController postController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(postController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("POST /api/posts/summarize - Valid postId returns summary")
    void summarizePost_ValidPostId_ReturnsSummary() throws Exception {
        // Arrange
        String postId = "test-post-123";
        String expectedSummary = "This is a comprehensive summary of the post content.";
        PostSummaryRequest request = new PostSummaryRequest(postId);
        PostSummaryResponse response = PostSummaryResponse.success(expectedSummary, false);

        when(postService.summarizePost(postId)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/posts/summarize")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.summary").value(expectedSummary))
                .andExpect(jsonPath("$.cached").value(false))
                .andExpect(jsonPath("$.errorMessage").doesNotExist());

        verify(postService, times(1)).summarizePost(postId);
    }

    @Test
    @DisplayName("POST /api/posts/summarize - Non-existent postId returns error")
    void summarizePost_NonExistentPostId_ReturnsError() throws Exception {
        // Arrange
        String postId = "non-existent-post";
        PostSummaryRequest request = new PostSummaryRequest(postId);
        PostSummaryResponse response = PostSummaryResponse.error("Post not found");

        when(postService.summarizePost(postId)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/posts/summarize")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorMessage").value("Post not found"))
                .andExpect(jsonPath("$.summary").doesNotExist());

        verify(postService, times(1)).summarizePost(postId);
    }

    @Test
    @DisplayName("POST /api/posts/summarize - Empty postId returns error")
    void summarizePost_EmptyPostId_ReturnsError() throws Exception {
        // Arrange
        PostSummaryRequest request = new PostSummaryRequest("");
        PostSummaryResponse response = PostSummaryResponse.error("Post not found");

        when(postService.summarizePost("")).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/posts/summarize")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false));

        verify(postService, times(1)).summarizePost("");
    }

    @Test
    @DisplayName("POST /api/posts/summarize - Cached response returns cached flag")
    void summarizePost_CachedResponse_ReturnsCachedFlag() throws Exception {
        // Arrange
        String postId = "cached-post-123";
        String cachedSummary = "This summary was retrieved from cache.";
        PostSummaryRequest request = new PostSummaryRequest(postId);
        PostSummaryResponse response = PostSummaryResponse.success(cachedSummary, true);

        when(postService.summarizePost(postId)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/posts/summarize")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.summary").value(cachedSummary))
                .andExpect(jsonPath("$.cached").value(true));

        verify(postService, times(1)).summarizePost(postId);
    }

    @Test
    @DisplayName("POST /api/posts/summarize - AI service failure returns error")
    void summarizePost_AIServiceFailure_ReturnsError() throws Exception {
        // Arrange
        String postId = "test-post-456";
        PostSummaryRequest request = new PostSummaryRequest(postId);
        PostSummaryResponse response = PostSummaryResponse.error("Failed to generate summary: AI service unavailable");

        when(postService.summarizePost(postId)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/posts/summarize")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorMessage").value("Failed to generate summary: AI service unavailable"));

        verify(postService, times(1)).summarizePost(postId);
    }

    @Test
    @DisplayName("POST /api/posts/summarize - Long content handled gracefully")
    void summarizePost_LongContent_HandledGracefully() throws Exception {
        // Arrange
        String postId = "long-content-post";
        String longSummary = "This summary was generated from a very long post. ".repeat(10);
        PostSummaryRequest request = new PostSummaryRequest(postId);
        PostSummaryResponse response = PostSummaryResponse.success(longSummary, false);

        when(postService.summarizePost(postId)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/posts/summarize")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.summary").isNotEmpty());

        verify(postService, times(1)).summarizePost(postId);
    }

    @Test
    @DisplayName("POST /api/posts/summarize - Unicode content in summary")
    void summarizePost_UnicodeContent_HandledCorrectly() throws Exception {
        // Arrange
        String postId = "unicode-post";
        String unicodeSummary = "这是一个测试摘要，包含中文内容。日本語も含まれています。🎉";
        PostSummaryRequest request = new PostSummaryRequest(postId);
        PostSummaryResponse response = PostSummaryResponse.success(unicodeSummary, false);

        when(postService.summarizePost(postId)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/posts/summarize")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.summary").value(unicodeSummary));

        verify(postService, times(1)).summarizePost(postId);
    }
}
