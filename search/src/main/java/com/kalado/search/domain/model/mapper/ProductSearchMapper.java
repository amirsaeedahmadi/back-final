package com.kalado.search.domain.model.mapper;

import com.kalado.common.dto.ProductDto;
import com.kalado.search.domain.model.ProductDocument;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.data.domain.Page;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface ProductSearchMapper {

    @Mapping(target = "id", expression = "java(String.valueOf(productDto.getId()))")
    @Mapping(target = "createdAt", source = "createdAt", qualifiedByName = "timestampToInstant")
    ProductDocument dtoToDocument(ProductDto productDto);

    @Mapping(target = "id", expression = "java(Long.valueOf(document.getId()))")
    @Mapping(target = "createdAt", source = "createdAt", qualifiedByName = "instantToTimestamp")
    ProductDto documentToDto(ProductDocument document);

    default Page<ProductDto> toProductDtoPage(Page<ProductDocument> page) {
        return page.map(this::documentToDto);
    }

    default List<ProductDto> toDtoList(List<ProductDocument> documents) {
        return documents.stream()
                .map(this::documentToDto)
                .collect(Collectors.toList());
    }

    @Named("timestampToInstant")
    default Instant timestampToInstant(Timestamp timestamp) {
        return timestamp != null ? timestamp.toInstant() : null;
    }

    @Named("instantToTimestamp")
    default Timestamp instantToTimestamp(Instant instant) {
        return instant != null ? Timestamp.from(instant) : null;
    }
}