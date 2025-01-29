//package com.kalado.product;
//
//import com.kalado.common.Price;
//import com.kalado.common.dto.AuthDto;
//import com.kalado.common.enums.CurrencyUnit;
//import com.kalado.common.enums.ProductStatus;
//import com.kalado.common.enums.Role;
//import com.kalado.common.exception.CustomException;
//import com.kalado.common.feign.authentication.AuthenticationApi;
//import com.kalado.common.feign.user.UserApi;
//import com.kalado.product.application.service.ProductService;
//import com.kalado.product.domain.model.Product;
//import com.kalado.product.infrastructure.repository.ProductRepository;
//import org.junit.jupiter.api.*;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.mock.web.MockMultipartFile;
//import org.springframework.test.context.DynamicPropertyRegistry;
//import org.springframework.test.context.DynamicPropertySource;
//import org.springframework.transaction.annotation.Transactional;
//import org.springframework.web.multipart.MultipartFile;
//import org.testcontainers.containers.KafkaContainer;
//import org.testcontainers.junit.jupiter.Container;
//import org.testcontainers.junit.jupiter.Testcontainers;
//import org.testcontainers.utility.DockerImageName;
//
//import java.util.Arrays;
//import java.util.Collections;
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.*;
//import static org.mockito.Mockito.when;
//
//@SpringBootTest
//@Testcontainers
//@Transactional
//class ProductServiceIntegrationTest {
//
//    // Using the appropriate Kafka container
//    @Container
//    static KafkaContainer kafka = new KafkaContainer(
//            DockerImageName.parse("confluentinc/cp-kafka:latest") // Latest stable Confluent Kafka image
//    );
//
//    // Register Kafka properties dynamically
//    @DynamicPropertySource
//    static void kafkaProperties(DynamicPropertyRegistry registry) {
//        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
//    }
//
//    @Autowired
//    private ProductService productService;
//
//    @Autowired
//    private ProductRepository productRepository;
//
//    @MockBean
//    private AuthenticationApi authenticationApi;
//
//    @MockBean
//    private UserApi userApi;
//
//    private Product testProduct;
//    private List<MultipartFile> testImages;
//
//    @BeforeEach
//    void setUp() {
//        productRepository.deleteAll();
//
//        // Mock authentication API
//        when(authenticationApi.validate(anyString()))
//                .thenReturn(AuthDto.builder()
//                        .userId(100L)
//                        .role(Role.USER)
//                        .isValid(true)
//                        .build());
//
//        testProduct = Product.builder()
//                .title("Integration Test Product")
//                .description("Product for integration testing")
//                .category("Electronics")
//                .price(Price.builder()
//                        .amount(1000.0)
//                        .unit(CurrencyUnit.TOMAN)
//                        .build())
//                .sellerId(100L)
//                .status(ProductStatus.ACTIVE)
//                .build();
//
//        testImages = Collections.singletonList(
//                new MockMultipartFile(
//                        "image",
//                        "test.jpg",
//                        "image/jpeg",
//                        "test image content".getBytes()
//                )
//        );
//    }
//
//    @Nested
//    @DisplayName("Product Lifecycle Integration Tests")
//    class ProductLifecycleTests {
//        @Test
//        @DisplayName("Full Product Lifecycle Test")
//        void productLifecycle_ShouldSucceed() {
//            Product createdProduct = productService.createProduct(testProduct, testImages);
//            assertNotNull(createdProduct.getId());
//            assertEquals(ProductStatus.ACTIVE, createdProduct.getStatus());
//
//            Product updateData = createdProduct.toBuilder()
//                    .title("Updated Title")
//                    .price(Price.builder()
//                            .amount(1500.0)
//                            .unit(CurrencyUnit.TOMAN)
//                            .build())
//                    .build();
//
//            Product updatedProduct = productService.updateProduct(createdProduct.getId(), updateData, null);
//            assertEquals("Updated Title", updatedProduct.getTitle());
//            assertEquals(1500.0, updatedProduct.getPrice().getAmount());
//
//            Product reservedProduct = productService.updateProductStatus(
//                    createdProduct.getId(), ProductStatus.RESERVED, 100L);
//            assertEquals(ProductStatus.RESERVED, reservedProduct.getStatus());
//
//            productService.deleteProduct(createdProduct.getId(), 100L);
//            Product deletedProduct = productRepository.findById(createdProduct.getId())
//                    .orElseThrow();
//            assertEquals(ProductStatus.DELETED, deletedProduct.getStatus());
//        }
//    }
//
//    @Nested
//    @DisplayName("Product Search and Filtering Integration Tests")
//    class ProductSearchAndFilteringTests {
//        private List<Product> setupTestProducts() {
//            Product product1 = testProduct.toBuilder()
//                    .title("Smartphone")
//                    .category("Electronics")
//                    .price(Price.builder()
//                            .amount(1000.0)
//                            .unit(CurrencyUnit.TOMAN)
//                            .build())
//                    .build();
//
//            Product product2 = testProduct.toBuilder()
//                    .title("Laptop")
//                    .category("Electronics")
//                    .price(Price.builder()
//                            .amount(2000.0)
//                            .unit(CurrencyUnit.TOMAN)
//                            .build())
//                    .build();
//
//            Product product3 = testProduct.toBuilder()
//                    .title("Book")
//                    .category("Books")
//                    .price(Price.builder()
//                            .amount(50.0)
//                            .unit(CurrencyUnit.TOMAN)
//                            .build())
//                    .build();
//
//            return Arrays.asList(
//                    productService.createProduct(product1, null),
//                    productService.createProduct(product2, null),
//                    productService.createProduct(product3, null)
//            );
//        }
//
//        @Test
//        @DisplayName("Get Products by Category")
//        void getProductsByCategory_ShouldReturnCorrectProducts() {
//            List<Product> testProducts = setupTestProducts();
//
//            List<Product> electronicsProducts = productService.getProductsByCategory("Electronics");
//            assertEquals(2, electronicsProducts.size());
//            assertTrue(electronicsProducts.stream()
//                    .allMatch(p -> "Electronics".equals(p.getCategory())));
//
//            List<Product> bookProducts = productService.getProductsByCategory("Books");
//            assertEquals(1, bookProducts.size());
//            assertTrue(bookProducts.stream()
//                    .allMatch(p -> "Books".equals(p.getCategory())));
//        }
//
//        @Test
//        @DisplayName("Get Products by Seller")
//        void getProductsBySeller_ShouldReturnCorrectProducts() {
//            List<Product> testProducts = setupTestProducts();
//            long sellerId = 100L;
//
//            List<Product> sellerProducts = productService.getProductsBySeller(sellerId);
//            assertFalse(sellerProducts.isEmpty());
//            assertTrue(sellerProducts.stream()
//                    .allMatch(p -> p.getSellerId().equals(sellerId)));
//        }
//    }
//}
