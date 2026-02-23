package com.document.management.service;

import com.document.management.dto.DocumentMapper;
import com.document.management.dto.DocumentRequestDto;
import com.document.management.dto.DocumentResponseDto;
import com.document.management.entity.Document;
import com.document.management.exception.ResourceNotFoundException;
import com.document.management.repository.DocumentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private DocumentMapper documentMapper;

    @InjectMocks
    private DocumentService documentService;

    private Document document;
    private DocumentRequestDto requestDto;
    private DocumentResponseDto responseDto;

    @BeforeEach
    void setUp() {
        document = Document.builder()
                .id(1L)
                .name("Test Document")
                .description("Test Description")
                .createdAt(LocalDateTime.now())
                .build();

        requestDto = DocumentRequestDto.builder()
                .name("Test Document")
                .description("Test Description")
                .build();

        responseDto = DocumentResponseDto.builder()
                .id(1L)
                .name("Test Document")
                .description("Test Description")
                .createdAt(document.getCreatedAt())
                .build();
    }

    @Nested
    @DisplayName("getAllDocuments")
    class GetAllDocuments {

        @Test
        @DisplayName("should return list of documents")
        void shouldReturnListOfDocuments() {
            when(documentRepository.findAll()).thenReturn(List.of(document));
            when(documentMapper.toResponseDto(document)).thenReturn(responseDto);

            List<DocumentResponseDto> result = documentService.getAllDocuments();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getName()).isEqualTo("Test Document");
            verify(documentRepository).findAll();
        }

        @Test
        @DisplayName("should return empty list when no documents exist")
        void shouldReturnEmptyList() {
            when(documentRepository.findAll()).thenReturn(Collections.emptyList());

            List<DocumentResponseDto> result = documentService.getAllDocuments();

            assertThat(result).isEmpty();
            verify(documentRepository).findAll();
        }
    }

    @Nested
    @DisplayName("getDocumentById")
    class GetDocumentById {

        @Test
        @DisplayName("should return document when found")
        void shouldReturnDocumentWhenFound() {
            when(documentRepository.findById(1L)).thenReturn(Optional.of(document));
            when(documentMapper.toResponseDto(document)).thenReturn(responseDto);

            DocumentResponseDto result = documentService.getDocumentById(1L);

            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getName()).isEqualTo("Test Document");
            verify(documentRepository).findById(1L);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when not found")
        void shouldThrowWhenNotFound() {
            when(documentRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> documentService.getDocumentById(99L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Document not found with id: 99");
        }
    }

    @Nested
    @DisplayName("createDocument")
    class CreateDocument {

        @Test
        @DisplayName("should create and return document")
        void shouldCreateAndReturnDocument() {
            when(documentMapper.toEntity(requestDto)).thenReturn(document);
            when(documentRepository.save(document)).thenReturn(document);
            when(documentMapper.toResponseDto(document)).thenReturn(responseDto);

            DocumentResponseDto result = documentService.createDocument(requestDto);

            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getName()).isEqualTo("Test Document");
            verify(documentRepository).save(document);
        }
    }

    @Nested
    @DisplayName("updateDocument")
    class UpdateDocument {

        @Test
        @DisplayName("should update and return document when found")
        void shouldUpdateAndReturnDocument() {
            DocumentRequestDto updateDto = DocumentRequestDto.builder()
                    .name("Updated Name")
                    .description("Updated Description")
                    .build();
            DocumentResponseDto updatedResponse = DocumentResponseDto.builder()
                    .id(1L)
                    .name("Updated Name")
                    .description("Updated Description")
                    .createdAt(document.getCreatedAt())
                    .build();

            when(documentRepository.findById(1L)).thenReturn(Optional.of(document));
            when(documentRepository.save(document)).thenReturn(document);
            when(documentMapper.toResponseDto(document)).thenReturn(updatedResponse);

            DocumentResponseDto result = documentService.updateDocument(1L, updateDto);

            assertThat(result.getName()).isEqualTo("Updated Name");
            verify(documentMapper).updateEntityFromDto(updateDto, document);
            verify(documentRepository).save(document);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when not found")
        void shouldThrowWhenNotFound() {
            when(documentRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> documentService.updateDocument(99L, requestDto))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Document not found with id: 99");

            verify(documentRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("deleteDocument")
    class DeleteDocument {

        @Test
        @DisplayName("should delete document when found")
        void shouldDeleteWhenFound() {
            when(documentRepository.existsById(1L)).thenReturn(true);

            documentService.deleteDocument(1L);

            verify(documentRepository).deleteById(1L);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when not found")
        void shouldThrowWhenNotFound() {
            when(documentRepository.existsById(99L)).thenReturn(false);

            assertThatThrownBy(() -> documentService.deleteDocument(99L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Document not found with id: 99");

            verify(documentRepository, never()).deleteById(any());
        }
    }
}
