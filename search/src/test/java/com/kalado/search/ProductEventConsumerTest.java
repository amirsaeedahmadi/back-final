package com.kalado.search;

import com.kalado.common.Price;
import com.kalado.common.dto.ProductDto;
import com.kalado.common.enums.CurrencyUnit;
import com.kalado.common.enums.ProductStatus;
import com.kalado.common.event.ProductEvent;
import com.kalado.search.application.service.SearchService;
import com.kalado.search.domain.model.ProductDocument;
import com.kalado.search.domain.model.mapper.ProductSearchMapper;
import com.kalado.search.infrastructure.messaging.ProductEventConsumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductEventConsumerTest {

    @Mock
    private SearchService searchService;

    @Mock
    private ProductSearchMapper productSearchMapper;

    @InjectMocks
    private ProductEventConsumer productEventConsumer;

    @Captor
    private ArgumentCaptor<ProductDocument> documentCaptor;

    private ProductDto sampleProductDto;
    private ProductDocument sampleDocument;

    @BeforeEach
    void setUp() {
        sampleProductDto = ProductDto.builder()
                .id(1L)
                .title("Test Product")
                .description("Test Description")
                .price(new Price(1000.0, CurrencyUnit.TOMAN))
                .imageUrls(Arrays.asList("image1.jpg", "image2.jpg"))
                .category("Electronics")
                .productionYear(2024)
                .brand("Test Brand")
                .status(ProductStatus.ACTIVE)
                .createdAt(new Timestamp(System.currentTimeMillis()))
                .sellerId(1L)
                .build();

        sampleDocument = ProductDocument.builder()
                .id("1")
                .title("Test Product")
                .description("Test Description")
                .price(new Price(1000.0, CurrencyUnit.TOMAN))
                .category("Electronics")
                .status(ProductStatus.ACTIVE)
                .createdAt(Instant.now())
                .sellerId(1L)
                .build();
    }

    @Test
    @DisplayName("Handle CREATED event should index new product")
    void handleProductEvent_Created_ShouldIndexProduct() {
        ProductEvent event = new ProductEvent("CREATED", sampleProductDto);
        when(productSearchMapper.dtoToDocument(sampleProductDto)).thenReturn(sampleDocument);
        doNothing().when(searchService).indexProduct(any(ProductDocument.class));

        productEventConsumer.handleProductEvent(event);

        verify(productSearchMapper).dtoToDocument(sampleProductDto);
        verify(searchService).indexProduct(documentCaptor.capture());

        ProductDocument capturedDocument = documentCaptor.getValue();
        assertEquals(sampleDocument.getId(), capturedDocument.getId());
        assertEquals(sampleDocument.getTitle(), capturedDocument.getTitle());
    }

    @Test
    @DisplayName("Handle DELETED event should remove product from index")
    void handleProductEvent_Deleted_ShouldRemoveProduct() {
        ProductEvent event = new ProductEvent("DELETED", sampleProductDto);
        doNothing().when(searchService).deleteProduct(any(String.class));

        productEventConsumer.handleProductEvent(event);

        verify(searchService).deleteProduct(String.valueOf(sampleProductDto.getId()));
        verify(productSearchMapper, never()).dtoToDocument(any());
    }

    @Test
    @DisplayName("Handle unknown event type should log warning")
    void handleProductEvent_UnknownType_ShouldLogWarning() {
        ProductEvent event = new ProductEvent("UNKNOWN_TYPE", sampleProductDto);

        productEventConsumer.handleProductEvent(event);

        verify(searchService, never()).indexProduct(any());
        verify(searchService, never()).deleteProduct(any());
        verify(productSearchMapper, never()).dtoToDocument(any());
    }

    @Test
    @DisplayName("Handle event with service exception should propagate exception")
    void handleProductEvent_ServiceException_ShouldPropagateException() {
        ProductEvent event = new ProductEvent("CREATED", sampleProductDto);
        when(productSearchMapper.dtoToDocument(sampleProductDto)).thenReturn(sampleDocument);
        doThrow(new RuntimeException("Test exception")).when(searchService).indexProduct(any());

        assertThrows(RuntimeException.class, () -> productEventConsumer.handleProductEvent(event));
    }
}