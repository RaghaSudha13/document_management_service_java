package com.document.management.service;

import com.document.management.dto.DocumentMapper;
import com.document.management.dto.DocumentRequestDto;
import com.document.management.dto.DocumentResponseDto;
import com.document.management.entity.Document;
import com.document.management.exception.ResourceNotFoundException;
import com.document.management.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final DocumentMapper documentMapper;

    public List<DocumentResponseDto> getAllDocuments() {
        return documentRepository.findAll()
                .stream()
                .map(documentMapper::toResponseDto)
                .toList();
    }

    public DocumentResponseDto getDocumentById(Long id) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document", id));
        return documentMapper.toResponseDto(document);
    }

    @Transactional
    public DocumentResponseDto createDocument(DocumentRequestDto requestDto) {
        Document document = documentMapper.toEntity(requestDto);
        Document saved = documentRepository.save(document);
        return documentMapper.toResponseDto(saved);
    }

    @Transactional
    public DocumentResponseDto updateDocument(Long id, DocumentRequestDto requestDto) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document", id));
        documentMapper.updateEntityFromDto(requestDto, document);
        Document updated = documentRepository.save(document);
        return documentMapper.toResponseDto(updated);
    }

    @Transactional
    public void deleteDocument(Long id) {
        if (!documentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Document", id);
        }
        documentRepository.deleteById(id);
    }
}
