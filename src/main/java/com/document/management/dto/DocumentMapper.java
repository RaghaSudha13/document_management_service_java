package com.document.management.dto;

import com.document.management.entity.Document;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface DocumentMapper {

    Document toEntity(DocumentRequestDto dto);

    DocumentResponseDto toResponseDto(Document entity);

    void updateEntityFromDto(DocumentRequestDto dto, @MappingTarget Document entity);
}
