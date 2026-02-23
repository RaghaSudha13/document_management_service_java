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
import java.util.UUID;

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

    private static final UUID DOC_ID = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
    private static final UUID NON_EXISTENT_ID = UUID.fromString("00000000-0000-0000-0000-000000000099");
    private static final LocalDateTime FIXED_TIME = LocalDateTime.of(2026, 2, 23, 10, 0);

    private Document document;
    private DocumentRequestDto requestDto;
    private DocumentResponseDto responseDto;

    @BeforeEach
    void setUp() {
        document = Document.builder()
                .id(DOC_ID)
                .name("Test Document")
                .description("Test Description")
                .createdAt(FIXED_TIME)
                .build();

        requestDto = DocumentRequestDto.builder()
                .name("Test Document")
                .description("Test Description")
                .build();

        responseDto = DocumentResponseDto.builder()
                .id(DOC_ID)
                .name("Test Document")
                .description("Test Description")
                .createdAt(FIXED_TIME)
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
            when(documentRepository.findById(DOC_ID)).thenReturn(Optional.of(document));
            when(documentMapper.toResponseDto(document)).thenReturn(responseDto);

            DocumentResponseDto result = documentService.getDocumentById(DOC_ID);

            assertThat(result.getId()).isEqualTo(DOC_ID);
            assertThat(result.getName()).isEqualTo("Test Document");
            verify(documentRepository).findById(DOC_ID);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when not found")
        void shouldThrowWhenNotFound() {
            when(documentRepository.findById(NON_EXISTENT_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> documentService.getDocumentById(NON_EXISTENT_ID))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Document not found with id: " + NON_EXISTENT_ID);
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

            assertThat(result.getId()).isEqualTo(DOC_ID);
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
                    .id(DOC_ID)
                    .name("Updated Name")
                    .description("Updated Description")
                    .createdAt(FIXED_TIME)
                    .build();

            when(documentRepository.findById(DOC_ID)).thenReturn(Optional.of(document));
            when(documentRepository.save(document)).thenReturn(document);
            when(documentMapper.toResponseDto(document)).thenReturn(updatedResponse);

            DocumentResponseDto result = documentService.updateDocument(DOC_ID, updateDto);

            assertThat(result.getName()).isEqualTo("Updated Name");
            verify(documentMapper).updateEntityFromDto(updateDto, document);
            verify(documentRepository).save(document);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when not found")
        void shouldThrowWhenNotFound() {
            when(documentRepository.findById(NON_EXISTENT_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> documentService.updateDocument(NON_EXISTENT_ID, requestDto))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Document not found with id: " + NON_EXISTENT_ID);

            verify(documentRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("deleteDocument")
    class DeleteDocument {

        @Test
        @DisplayName("should delete document when found")
        void shouldDeleteWhenFound() {
            when(documentRepository.existsById(DOC_ID)).thenReturn(true);

            documentService.deleteDocument(DOC_ID);

            verify(documentRepository).deleteById(DOC_ID);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when not found")
        void shouldThrowWhenNotFound() {
            when(documentRepository.existsById(NON_EXISTENT_ID)).thenReturn(false);

            assertThatThrownBy(() -> documentService.deleteDocument(NON_EXISTENT_ID))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Document not found with id: " + NON_EXISTENT_ID);

            verify(documentRepository, never()).deleteById(any());
        }
    }
}
