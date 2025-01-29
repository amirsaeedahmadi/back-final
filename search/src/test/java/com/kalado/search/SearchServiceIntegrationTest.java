//package com.kalado.search;
//
//import com.kalado.common.Price;
//import com.kalado.common.dto.ProductDto;
//import com.kalado.common.enums.CurrencyUnit;
//import com.kalado.common.enums.ProductStatus;
//import com.kalado.common.event.ProductEvent;
//import com.kalado.search.application.service.SearchService;
//import com.kalado.search.domain.model.ProductDocument;
//import com.kalado.search.domain.model.mapper.ProductSearchMapper;
//import com.kalado.search.infrastructure.messaging.ProductEventConsumer;
//import com.kalado.search.infrastructure.repository.ProductSearchRepository;
//import org.elasticsearch.search.sort.SortOrder;
//import org.junit.jupiter.api.*;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
//import org.springframework.test.context.DynamicPropertyRegistry;
//import org.springframework.test.context.DynamicPropertySource;
//import org.testcontainers.junit.jupiter.Container;
//import org.testcontainers.junit.jupiter.Testcontainers;
//import org.testcontainers.elasticsearch.ElasticsearchContainer;
//
//
//import java.time.Instant;
//import java.util.Arrays;
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//@SpringBootTest
//@Testcontainers
//class SearchServiceIntegrationTest {
//
//    @Container
//    static final ElasticsearchContainer elasticsearchContainer =
//            new ElasticsearchContainer("docker.elastic.co/elasticsearch/elasticsearch:7.17.9")
//                    .withPassword("kalado_pass")
//                    .withEnv("discovery.type", "single-node")
//                    .withEnv("ES_JAVA_OPTS", "-Xms512m -Xmx512m")
//                    .withEnv("xpack.security.enabled", "true")
//                    .withExposedPorts(9200)
//                    .withEnv("ELASTIC_PASSWORD", "kalado_pass");
//
//    @DynamicPropertySource
//    static void elasticsearchProperties(DynamicPropertyRegistry registry) {
//        registry.add("spring.elasticsearch.uris",
//                () -> elasticsearchContainer.getHttpHostAddress());
//        registry.add("spring.elasticsearch.username", () -> "elastic");
//        registry.add("spring.elasticsearch.password", () -> "kalado_pass");
//    }
//
//    @Autowired
//    private SearchService searchService;
//
//    @Autowired
//    private ProductSearchRepository searchRepository;
//
//    @Autowired
//    private ProductEventConsumer eventConsumer;
//
//    @Autowired
//    private ElasticsearchOperations elasticsearchOperations;
//
//    @Autowired
//    private ProductSearchMapper productSearchMapper;
//
//    private ProductDocument testProduct1;
//    private ProductDocument testProduct2;
//    private ProductDocument testProduct3;
//
//    @BeforeEach
//    void setUp() {
//        // Clear the index
//        searchRepository.deleteAll();
//
//        // Create test products
//        testProduct1 = ProductDocument.builder()
//                .id("1")
//                .title("iPhone 14 Pro")
//                .description("Latest Apple smartphone")
//                .price(new Price(999.99, CurrencyUnit.TOMAN))
//                .category("Electronics")
//                .productionYear(2023)
//                .brand("Apple")
//                .status(ProductStatus.ACTIVE)
//                .createdAt(Instant.now())
//                .sellerId(1L)
//                .build();
//
//        testProduct2 = ProductDocument.builder()
//                .id("2")
//                .title("Samsung Galaxy S23")
//                .description("Premium Android smartphone")
//                .price(new Price(899.99, CurrencyUnit.TOMAN))
//                .category("Electronics")
//                .productionYear(2023)
//                .brand("Samsung")
//                .status(ProductStatus.ACTIVE)
//                .createdAt(Instant.now().minusSeconds(86400)) // 1 day old
//                .sellerId(2L)
//                .build();
//
//        testProduct3 = ProductDocument.builder()
//                .id("3")
//                .title("MacBook Pro")
//                .description("Professional laptop")
//                .price(new Price(1499.99, CurrencyUnit.TOMAN))
//                .category("Electronics")
//                .productionYear(2023)
//                .brand("Apple")
//                .status(ProductStatus.ACTIVE)
//                .createdAt(Instant.now().minusSeconds(172800)) // 2 days old
//                .sellerId(1L)
//                .build();
//
//        // Index test products
//        searchRepository.saveAll(Arrays.asList(testProduct1, testProduct2, testProduct3));
//
//        // Wait for indexing
//        try {
//            Thread.sleep(1000);
//        } catch (InterruptedException e) {
//            Thread.currentThread().interrupt();
//        }
//    }
//
//    @Test
//    @DisplayName("Search by keyword should return relevant results")
//    void searchByKeyword_ShouldReturnRelevantResults() {
//        // Act
//        Page<ProductDocument> result = searchService.searchProducts(
//                "iPhone", null, null, null, "date",
//                SortOrder.DESC, PageRequest.of(0, 10)
//        );
//
//        // Assert
//        assertEquals(1, result.getTotalElements());
//        assertTrue(result.getContent().get(0).getTitle().contains("iPhone"));
//    }
//
//    @Test
//    @DisplayName("Search by price range should filter correctly")
//    void searchByPriceRange_ShouldFilterCorrectly() {
//        // Act
//        Page<ProductDocument> result = searchService.searchProducts(
//                null, 800.0, 1000.0, null, "price",
//                SortOrder.ASC, PageRequest.of(0, 10)
//        );
//
//        // Assert
//        assertEquals(2, result.getTotalElements());
//        assertTrue(result.getContent().get(0).getPrice().getAmount() >= 800.0);
//        assertTrue(result.getContent().get(0).getPrice().getAmount() <= 1000.0);
//    }
//
//    @Test
//    @DisplayName("Search with time filter should return recent products")
//    void searchWithTimeFilter_ShouldReturnRecentProducts() {
//        // Act
//        Page<ProductDocument> result = searchService.searchProducts(
//                null, null, null, "1D", "date",
//                SortOrder.DESC, PageRequest.of(0, 10)
//        );
//
//        // Assert
//        assertEquals(1, result.getTotalElements());
//        assertTrue(result.getContent().get(0).getCreatedAt()
//                .isAfter(Instant.now().minusSeconds(86400)));
//    }
//
//    @Test
//    @DisplayName("Product event handling should update index correctly")
//    void productEventHandling_ShouldUpdateIndexCorrectly() {
//        // Arrange
//        ProductDto newProduct = ProductDto.builder()
//                .id(4L)
//                .title("iPad Pro")
//                .description("Professional tablet")
//                .price(new Price(799.99, CurrencyUnit.TOMAN))
//                .category("Electronics")
//                .status(ProductStatus.ACTIVE)
//                .sellerId(1L)
//                .build();
//
//        // Act - Create
//        eventConsumer.handleProductEvent(new ProductEvent("CREATED", newProduct));
//
//        // Assert - Create
//        assertTrue(searchRepository.findById("4").isPresent());
//
//        // Act - Update
//        newProduct.setTitle("iPad Pro 2024");
//        eventConsumer.handleProductEvent(new ProductEvent("UPDATED", newProduct));
//
//        // Assert - Update
//        assertEquals("iPad Pro 2024",
//                searchRepository.findById("4").get().getTitle());
//
//        // Act - Delete
//        eventConsumer.handleProductEvent(new ProductEvent("DELETED", newProduct));
//
//        // Assert - Delete
//        assertFalse(searchRepository.findById("4").isPresent());
//    }
//
//    @Test
//    @DisplayName("Sorting should work correctly")
//    void sorting_ShouldWorkCorrectly() {
//        // Act - Sort by price ascending
//        Page<ProductDocument> resultAsc = searchService.searchProducts(
//                null, null, null, null, "price",
//                SortOrder.ASC, PageRequest.of(0, 10)
//        );
//
//        // Assert - Price ascending
//        List<ProductDocument> contentAsc = resultAsc.getContent();
//        assertTrue(contentAsc.get(0).getPrice().getAmount() <=
//                contentAsc.get(1).getPrice().getAmount());
//
//        // Act - Sort by price descending
//        Page<ProductDocument> resultDesc = searchService.searchProducts(
//                null, null, null, null, "price",
//                SortOrder.DESC, PageRequest.of(0, 10)
//        );
//
//        // Assert - Price descending
//        List<ProductDocument> contentDesc = resultDesc.getContent();
//        assertTrue(contentDesc.get(0).getPrice().getAmount() >=
//                contentDesc.get(1).getPrice().getAmount());
//    }
//
//    @Test
//    @DisplayName("Pagination should work correctly")
//    void pagination_ShouldWorkCorrectly() {
//        // Act - First page
//        Page<ProductDocument> firstPage = searchService.searchProducts(
//                null, null, null, null, "date",
//                SortOrder.DESC, PageRequest.of(0, 2)
//        );
//
//        // Assert - First page
//        assertEquals(2, firstPage.getContent().size());
//        assertEquals(3, firstPage.getTotalElements());
//        assertTrue(firstPage.hasNext());
//
//        // Act - Second page
//        Page<ProductDocument> secondPage = searchService.searchProducts(
//                null, null, null, null, "date",
//                SortOrder.DESC, PageRequest.of(1, 2)
//        );
//
//        // Assert - Second page
//        assertEquals(1, secondPage.getContent().size());
//        assertFalse(secondPage.hasNext());
//    }
//
//    @Test
//    @DisplayName("Multi-field search should work correctly")
//    void multiFieldSearch_ShouldWorkCorrectly() {
//        // Act - Search in title and description
//        Page<ProductDocument> result = searchService.searchProducts(
//                "smartphone", null, null, null, "date",
//                SortOrder.DESC, PageRequest.of(0, 10)
//        );
//
//        // Assert
//        assertEquals(2, result.getTotalElements());
//        assertTrue(result.getContent().stream()
//                .allMatch(p -> p.getTitle().contains("iPhone") ||
//                        p.getTitle().contains("Samsung") ||
//                        p.getDescription().contains("smartphone")));
//    }
//
//    @Test
//    @DisplayName("Brand-specific search should work correctly")
//    void brandSearch_ShouldWorkCorrectly() {
//        // Act
//        Page<ProductDocument> result = searchService.searchProducts(
//                "Apple", null, null, null, "date",
//                SortOrder.DESC, PageRequest.of(0, 10)
//        );
//
//        // Assert
//        assertEquals(2, result.getTotalElements());
//        assertTrue(result.getContent().stream()
//                .allMatch(p -> p.getBrand().equals("Apple")));
//    }
//}