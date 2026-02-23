package com.document.management.controller;

import com.document.management.config.SecurityConfig;
import com.document.management.dto.DocumentRequestDto;
import com.document.management.dto.DocumentResponseDto;
import com.document.management.exception.GlobalExceptionHandler;
import com.document.management.exception.ResourceNotFoundException;
import com.document.management.service.DocumentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DocumentController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class DocumentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DocumentService documentService;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String BASE_URL = "/api/v1/documents";

    private DocumentResponseDto responseDto;
    private DocumentRequestDto requestDto;

    @BeforeEach
    void setUp() {
        responseDto = DocumentResponseDto.builder()
                .id(1L)
                .name("Test Document")
                .description("Test Description")
                .createdAt(LocalDateTime.of(2026, 2, 23, 10, 0))
                .build();

        requestDto = DocumentRequestDto.builder()
                .name("Test Document")
                .description("Test Description")
                .build();
    }

    @Nested
    @DisplayName("GET /api/v1/documents")
    class GetAllDocuments {

        @Test
        @DisplayName("should return 200 with list of documents")
        void shouldReturnDocuments() throws Exception {
            when(documentService.getAllDocuments()).thenReturn(List.of(responseDto));

            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].id", is(1)))
                    .andExpect(jsonPath("$[0].name", is("Test Document")));
        }

        @Test
        @DisplayName("should return 200 with empty list")
        void shouldReturnEmptyList() throws Exception {
            when(documentService.getAllDocuments()).thenReturn(Collections.emptyList());

            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/documents/{id}")
    class GetDocumentById {

        @Test
        @DisplayName("should return 200 with document")
        void shouldReturnDocument() throws Exception {
            when(documentService.getDocumentById(1L)).thenReturn(responseDto);

            mockMvc.perform(get(BASE_URL + "/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(1)))
                    .andExpect(jsonPath("$.name", is("Test Document")))
                    .andExpect(jsonPath("$.description", is("Test Description")));
        }

        @Test
        @DisplayName("should return 404 when not found")
        void shouldReturn404WhenNotFound() throws Exception {
            when(documentService.getDocumentById(99L))
                    .thenThrow(new ResourceNotFoundException("Document", 99L));

            mockMvc.perform(get(BASE_URL + "/99"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status", is(404)))
                    .andExpect(jsonPath("$.message", is("Document not found with id: 99")));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/documents")
    class CreateDocument {

        @Test
        @DisplayName("should return 201 with created document")
        void shouldCreateDocument() throws Exception {
            when(documentService.createDocument(any(DocumentRequestDto.class))).thenReturn(responseDto);

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id", is(1)))
                    .andExpect(jsonPath("$.name", is("Test Document")));
        }

        @Test
        @DisplayName("should return 400 when name is blank")
        void shouldReturn400WhenNameBlank() throws Exception {
            DocumentRequestDto invalid = DocumentRequestDto.builder()
                    .name("")
                    .description("desc")
                    .build();

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalid)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status", is(400)))
                    .andExpect(jsonPath("$.errors.name").exists());
        }

        @Test
        @DisplayName("should return 400 when name is missing")
        void shouldReturn400WhenNameMissing() throws Exception {
            String json = "{\"description\": \"desc\"}";

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.name").exists());
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/documents/{id}")
    class UpdateDocument {

        @Test
        @DisplayName("should return 200 with updated document")
        void shouldUpdateDocument() throws Exception {
            DocumentResponseDto updatedResponse = DocumentResponseDto.builder()
                    .id(1L)
                    .name("Updated Name")
                    .description("Updated Desc")
                    .createdAt(responseDto.getCreatedAt())
                    .build();
            when(documentService.updateDocument(eq(1L), any(DocumentRequestDto.class)))
                    .thenReturn(updatedResponse);

            DocumentRequestDto updateDto = DocumentRequestDto.builder()
                    .name("Updated Name")
                    .description("Updated Desc")
                    .build();

            mockMvc.perform(put(BASE_URL + "/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name", is("Updated Name")));
        }

        @Test
        @DisplayName("should return 404 when not found")
        void shouldReturn404WhenNotFound() throws Exception {
            when(documentService.updateDocument(eq(99L), any(DocumentRequestDto.class)))
                    .thenThrow(new ResourceNotFoundException("Document", 99L));

            mockMvc.perform(put(BASE_URL + "/99")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("should return 400 when validation fails")
        void shouldReturn400WhenValidationFails() throws Exception {
            DocumentRequestDto invalid = DocumentRequestDto.builder()
                    .name("")
                    .build();

            mockMvc.perform(put(BASE_URL + "/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalid)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/documents/{id}")
    class DeleteDocument {

        @Test
        @DisplayName("should return 204 when deleted")
        void shouldDeleteDocument() throws Exception {
            doNothing().when(documentService).deleteDocument(1L);

            mockMvc.perform(delete(BASE_URL + "/1"))
                    .andExpect(status().isNoContent());

            verify(documentService).deleteDocument(1L);
        }

        @Test
        @DisplayName("should return 404 when not found")
        void shouldReturn404WhenNotFound() throws Exception {
            doThrow(new ResourceNotFoundException("Document", 99L))
                    .when(documentService).deleteDocument(99L);

            mockMvc.perform(delete(BASE_URL + "/99"))
                    .andExpect(status().isNotFound());
        }
    }
}
