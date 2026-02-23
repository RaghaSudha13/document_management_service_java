package com.document.management.controller;

import com.document.management.dto.DocumentRequestDto;
import com.document.management.dto.DocumentResponseDto;
import com.document.management.service.DocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/documents")
@RequiredArgsConstructor
@Tag(name = "Document", description = "Document management endpoints")
public class DocumentController {

    private final DocumentService documentService;

    @GetMapping
    @Operation(summary = "Get all documents")
    public ResponseEntity<List<DocumentResponseDto>> getAllDocuments() {
        return ResponseEntity.ok(documentService.getAllDocuments());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get document by ID")
    public ResponseEntity<DocumentResponseDto> getDocumentById(@PathVariable Long id) {
        return ResponseEntity.ok(documentService.getDocumentById(id));
    }

    @PostMapping
    @Operation(summary = "Create a new document")
    public ResponseEntity<DocumentResponseDto> createDocument(
            @Valid @RequestBody DocumentRequestDto requestDto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(documentService.createDocument(requestDto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing document")
    public ResponseEntity<DocumentResponseDto> updateDocument(
            @PathVariable Long id,
            @Valid @RequestBody DocumentRequestDto requestDto) {
        return ResponseEntity.ok(documentService.updateDocument(id, requestDto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a document")
    public ResponseEntity<Void> deleteDocument(@PathVariable Long id) {
        documentService.deleteDocument(id);
        return ResponseEntity.noContent().build();
    }
}
