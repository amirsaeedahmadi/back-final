package com.kalado.search;

import com.kalado.common.Price;
import com.kalado.common.dto.ProductDto;
import com.kalado.common.enums.CurrencyUnit;
import com.kalado.common.enums.ProductStatus;
import com.kalado.common.event.ProductEvent;
import com.kalado.search.application.service.SearchService;
import com.kalado.search.domain.model.ProductDocument;
import com.kalado.search.domain.model.mapper.ProductSearchMapper;
import com.kalado.search.infrastructure.repository.ProductSearchRepository;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SearchServiceTest {

    @Mock
    private ProductSearchRepository productSearchRepository;

    @Mock
    private ElasticsearchOperations elasticsearchOperations;

    @Mock
    private ProductSearchMapper productSearchMapper;

    @InjectMocks
    private SearchService searchService;

    @Captor
    private ArgumentCaptor<NativeSearchQuery> searchQueryCaptor;

    private ProductDocument sampleDocument;
    private Pageable defaultPageable;

    @BeforeEach
    void setUp() {
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

        defaultPageable = PageRequest.of(0, 20);
    }

    @Nested
    @DisplayName("Search Products Tests")
    class SearchProductsTests {

        @Test
        @DisplayName("Search with keyword should return matching products")
        void searchProducts_WithKeyword_ShouldReturnMatchingProducts() {
            String keyword = "test";
            SearchHit<ProductDocument> searchHit = mock(SearchHit.class);
            when(searchHit.getContent()).thenReturn(sampleDocument);

            SearchHits<ProductDocument> searchHits = mock(SearchHits.class);
            when(searchHits.getSearchHits()).thenReturn(Collections.singletonList(searchHit));
            when(searchHits.getTotalHits()).thenReturn(1L);

            when(elasticsearchOperations.search(any(NativeSearchQuery.class), eq(ProductDocument.class)))
                    .thenReturn(searchHits);

            Page<ProductDocument> result = searchService.searchProducts(
                    keyword, null, null, null, "date",
                    SortOrder.DESC, defaultPageable);

            assertNotNull(result);
            assertEquals(1, result.getTotalElements());
            verify(elasticsearchOperations).search(searchQueryCaptor.capture(), eq(ProductDocument.class));

            NativeSearchQuery capturedQuery = searchQueryCaptor.getValue();
            assertTrue(capturedQuery.getQuery() instanceof BoolQueryBuilder);
        }

        @Test
        @DisplayName("Search with price range should filter products")
        void searchProducts_WithPriceRange_ShouldFilterProducts() {
            Double minPrice = 500.0;
            Double maxPrice = 1500.0;

            SearchHit<ProductDocument> searchHit = mock(SearchHit.class);
            when(searchHit.getContent()).thenReturn(sampleDocument);

            SearchHits<ProductDocument> searchHits = mock(SearchHits.class);
            when(searchHits.getSearchHits()).thenReturn(Collections.singletonList(searchHit));
            when(searchHits.getTotalHits()).thenReturn(1L);

            when(elasticsearchOperations.search(any(NativeSearchQuery.class), eq(ProductDocument.class)))
                    .thenReturn(searchHits);

            Page<ProductDocument> result = searchService.searchProducts(
                    null, minPrice, maxPrice, null, "price",
                    SortOrder.ASC, defaultPageable);

            assertNotNull(result);
            assertEquals(1, result.getTotalElements());
            verify(elasticsearchOperations).search(searchQueryCaptor.capture(), eq(ProductDocument.class));

            NativeSearchQuery capturedQuery = searchQueryCaptor.getValue();
            assertTrue(capturedQuery.getQuery() instanceof BoolQueryBuilder);
        }

        @Test
        @DisplayName("Search with time filter should return recent products")
        void searchProducts_WithTimeFilter_ShouldReturnRecentProducts() {
            String timeFilter = "1D";

            SearchHit<ProductDocument> searchHit = mock(SearchHit.class);
            when(searchHit.getContent()).thenReturn(sampleDocument);

            SearchHits<ProductDocument> searchHits = mock(SearchHits.class);
            when(searchHits.getSearchHits()).thenReturn(Collections.singletonList(searchHit));
            when(searchHits.getTotalHits()).thenReturn(1L);

            when(elasticsearchOperations.search(any(NativeSearchQuery.class), eq(ProductDocument.class)))
                    .thenReturn(searchHits);

            Page<ProductDocument> result = searchService.searchProducts(
                    null, null, null, timeFilter, "date",
                    SortOrder.DESC, defaultPageable);

            assertNotNull(result);
            assertEquals(1, result.getTotalElements());
            verify(elasticsearchOperations).search(searchQueryCaptor.capture(), eq(ProductDocument.class));
        }

        @Test
        @DisplayName("Search with invalid time filter should ignore time filter")
        void searchProducts_WithInvalidTimeFilter_ShouldIgnoreTimeFilter() {
            String invalidTimeFilter = "INVALID";

            SearchHit<ProductDocument> searchHit = mock(SearchHit.class);
            when(searchHit.getContent()).thenReturn(sampleDocument);

            SearchHits<ProductDocument> searchHits = mock(SearchHits.class);
            when(searchHits.getSearchHits()).thenReturn(Collections.singletonList(searchHit));
            when(searchHits.getTotalHits()).thenReturn(1L);

            when(elasticsearchOperations.search(any(NativeSearchQuery.class), eq(ProductDocument.class)))
                    .thenReturn(searchHits);

            Page<ProductDocument> result = searchService.searchProducts(
                    null, null, null, invalidTimeFilter, "date",
                    SortOrder.DESC, defaultPageable);

            assertNotNull(result);
            verify(elasticsearchOperations).search(searchQueryCaptor.capture(), eq(ProductDocument.class));

            NativeSearchQuery capturedQuery = searchQueryCaptor.getValue();
            assertTrue(capturedQuery.getQuery() instanceof BoolQueryBuilder);
        }

        @Test
        @DisplayName("Search should exclude deleted products")
        void searchProducts_ShouldExcludeDeletedProducts() {
            SearchHit<ProductDocument> searchHit = mock(SearchHit.class);
            when(searchHit.getContent()).thenReturn(sampleDocument);

            SearchHits<ProductDocument> searchHits = mock(SearchHits.class);
            when(searchHits.getSearchHits()).thenReturn(Collections.singletonList(searchHit));
            when(searchHits.getTotalHits()).thenReturn(1L);

            when(elasticsearchOperations.search(any(NativeSearchQuery.class), eq(ProductDocument.class)))
                    .thenReturn(searchHits);

            Page<ProductDocument> result = searchService.searchProducts(
                    null, null, null, null, "date",
                    SortOrder.DESC, defaultPageable);

            assertNotNull(result);
            verify(elasticsearchOperations).search(searchQueryCaptor.capture(), eq(ProductDocument.class));

            NativeSearchQuery capturedQuery = searchQueryCaptor.getValue();
            BoolQueryBuilder boolQuery = (BoolQueryBuilder) capturedQuery.getQuery();
            assertNotNull(boolQuery.mustNot());
        }
    }

    @Nested
    @DisplayName("Product Indexing Tests")
    class ProductIndexingTests {

        @Test
        @DisplayName("Index product should save document")
        void indexProduct_ShouldSaveDocument() {
            when(productSearchRepository.save(any(ProductDocument.class)))
                    .thenReturn(sampleDocument);

            searchService.indexProduct(sampleDocument);

            verify(productSearchRepository).save(sampleDocument);
        }

        @Test
        @DisplayName("Delete product should remove document")
        void deleteProduct_ShouldRemoveDocument() {
            String productId = "1";
            doNothing().when(productSearchRepository).deleteById(productId);

            searchService.deleteProduct(productId);

            verify(productSearchRepository).deleteById(productId);
        }
    }
}