package com.kalado.search;

import com.kalado.common.Price;
import com.kalado.common.dto.ProductDto;
import com.kalado.common.enums.CurrencyUnit;
import com.kalado.common.enums.ProductStatus;
import com.kalado.search.domain.model.ProductDocument;
import com.kalado.search.domain.model.mapper.ProductSearchMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProductSearchMapperTest {

    private ProductSearchMapper mapper;
    private ProductDto sampleProductDto;
    private ProductDocument sampleDocument;
    private Price samplePrice;

    @BeforeEach
    void setUp() {
        mapper = Mappers.getMapper(ProductSearchMapper.class);

        samplePrice = new Price(1000.0, CurrencyUnit.TOMAN);
        Instant now = Instant.now();

        sampleProductDto = ProductDto.builder()
                .id(1L)
                .title("Test Product")
                .description("Test Description")
                .price(samplePrice)
                .imageUrls(Arrays.asList("image1.jpg", "image2.jpg"))
                .category("Electronics")
                .productionYear(2024)
                .brand("Test Brand")
                .status(ProductStatus.ACTIVE)
                .createdAt(Timestamp.from(now))
                .sellerId(1L)
                .build();

        sampleDocument = ProductDocument.builder()
                .id("1")
                .title("Test Product")
                .description("Test Description")
                .price(samplePrice)
                .imageUrls(Arrays.asList("image1.jpg", "image2.jpg"))
                .category("Electronics")
                .productionYear(2024)
                .brand("Test Brand")
                .status(ProductStatus.ACTIVE)
                .createdAt(now)
                .sellerId(1L)
                .build();
    }

    @Test
    @DisplayName("Map ProductDTO to ProductDocument")
    void dtoToDocument_ShouldMapAllFields() {
        ProductDocument document = mapper.dtoToDocument(sampleProductDto);

        assertNotNull(document);
        assertEquals(String.valueOf(sampleProductDto.getId()), document.getId());
        assertEquals(sampleProductDto.getTitle(), document.getTitle());
        assertEquals(sampleProductDto.getDescription(), document.getDescription());
        assertEquals(sampleProductDto.getPrice(), document.getPrice());
        assertEquals(sampleProductDto.getImageUrls(), document.getImageUrls());
        assertEquals(sampleProductDto.getCategory(), document.getCategory());
        assertEquals(sampleProductDto.getProductionYear(), document.getProductionYear());
        assertEquals(sampleProductDto.getBrand(), document.getBrand());
        assertEquals(sampleProductDto.getStatus(), document.getStatus());
        assertEquals(sampleProductDto.getCreatedAt().toInstant(), document.getCreatedAt());
        assertEquals(sampleProductDto.getSellerId(), document.getSellerId());
    }

    @Test
    @DisplayName("Map ProductDocument to ProductDTO")
    void documentToDto_ShouldMapAllFields() {
        ProductDto dto = mapper.documentToDto(sampleDocument);

        assertNotNull(dto);
        assertEquals(Long.valueOf(sampleDocument.getId()), dto.getId());
        assertEquals(sampleDocument.getTitle(), dto.getTitle());
        assertEquals(sampleDocument.getDescription(), dto.getDescription());
        assertEquals(sampleDocument.getPrice(), dto.getPrice());
        assertEquals(sampleDocument.getImageUrls(), dto.getImageUrls());
        assertEquals(sampleDocument.getCategory(), dto.getCategory());
        assertEquals(sampleDocument.getProductionYear(), dto.getProductionYear());
        assertEquals(sampleDocument.getBrand(), dto.getBrand());
        assertEquals(sampleDocument.getStatus(), dto.getStatus());
        assertEquals(sampleDocument.getCreatedAt(), dto.getCreatedAt().toInstant());
        assertEquals(sampleDocument.getSellerId(), dto.getSellerId());
    }

    @Test
    @DisplayName("Map Page of ProductDocuments to Page of ProductDTOs")
    void toProductDtoPage_ShouldMapPageCorrectly() {
        List<ProductDocument> documents = Collections.singletonList(sampleDocument);
        Page<ProductDocument> page = new PageImpl<>(documents, PageRequest.of(0, 10), 1);

        Page<ProductDto> dtoPage = mapper.toProductDtoPage(page);

        assertNotNull(dtoPage);
        assertEquals(page.getTotalElements(), dtoPage.getTotalElements());
        assertEquals(page.getSize(), dtoPage.getSize());
        assertEquals(page.getNumber(), dtoPage.getNumber());

        ProductDto mappedDto = dtoPage.getContent().get(0);
        assertEquals(Long.valueOf(sampleDocument.getId()), mappedDto.getId());
        assertEquals(sampleDocument.getTitle(), mappedDto.getTitle());
    }

    @Test
    @DisplayName("Handle null values in ProductDTO")
    void dtoToDocument_ShouldHandleNullValues() {
        ProductDto nullDto = ProductDto.builder()
                .id(1L)
                .title("Test Product")
                .build();

        ProductDocument document = mapper.dtoToDocument(nullDto);

        assertNotNull(document);
        assertEquals("1", document.getId());
        assertEquals("Test Product", document.getTitle());
        assertNull(document.getDescription());
        assertNull(document.getPrice());
        assertNull(document.getImageUrls());
        assertNull(document.getCategory());
        assertNull(document.getProductionYear());
        assertNull(document.getBrand());
        assertNull(document.getStatus());
        assertNull(document.getCreatedAt());
        assertNull(document.getSellerId());
    }

    @Test
    @DisplayName("Handle null values in ProductDocument")
    void documentToDto_ShouldHandleNullValues() {
        ProductDocument nullDocument = ProductDocument.builder()
                .id("1")
                .title("Test Product")
                .build();

        ProductDto dto = mapper.documentToDto(nullDocument);

        assertNotNull(dto);
        assertEquals(1L, dto.getId());
        assertEquals("Test Product", dto.getTitle());
        assertNull(dto.getDescription());
        assertNull(dto.getPrice());
        assertNull(dto.getImageUrls());
        assertNull(dto.getCategory());
        assertNull(dto.getProductionYear());
        assertNull(dto.getBrand());
        assertNull(dto.getStatus());
        assertNull(dto.getCreatedAt());
        assertNull(dto.getSellerId());
    }
}