package com.kalado.product;

import com.kalado.common.Price;
import com.kalado.common.dto.AuthDto;
import com.kalado.common.enums.CurrencyUnit;
import com.kalado.common.enums.ProductStatus;
import com.kalado.common.enums.Role;
import com.kalado.common.exception.CustomException;
import com.kalado.common.feign.authentication.AuthenticationApi;
import com.kalado.product.application.service.ImageService;
import com.kalado.product.application.service.ProductService;
import com.kalado.product.domain.model.Product;
import com.kalado.product.infrastructure.messaging.ProductEventPublisher;
import com.kalado.product.infrastructure.repository.ProductRepository;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ImageService imageService;

    @Mock
    private AuthenticationApi authenticationApi;

    @Mock
    private ProductEventPublisher eventPublisher;

    @InjectMocks
    private ProductService productService;

    @Captor
    private ArgumentCaptor<Product> productCaptor;

    private Product validProduct;
    private List<MultipartFile> validImages;

    @BeforeEach
    void setUp() {
        validProduct = Product.builder()
                .id(1L)
                .title("Test Product")
                .description("Test Description")
                .category("Electronics")
                .price(Price.builder()
                        .amount(1000.0)
                        .unit(CurrencyUnit.TOMAN)
                        .build())
                .sellerId(100L)
                .status(ProductStatus.ACTIVE)
                .build();

        validImages = Collections.singletonList(
                new MockMultipartFile(
                        "image",
                        "test.jpg",
                        "image/jpeg",
                        "test image content".getBytes()
                )
        );
    }

    @Nested
    @DisplayName("Product Creation Tests")
    class ProductCreationTests {
        @Test
        @DisplayName("Create Product Successfully with Images")
        void createProduct_WithImages_ShouldSucceed() {
            // Arrange
            List<String> imageUrls = Collections.singletonList("http://example.com/image.jpg");
            when(imageService.storeImages(any())).thenReturn(imageUrls);
            when(productRepository.save(any(Product.class))).thenReturn(validProduct);

            // Act
            Product result = productService.createProduct(validProduct, validImages);

            // Assert
            assertNotNull(result);
            verify(imageService).storeImages(validImages);
            verify(productRepository).save(productCaptor.capture());
            verify(eventPublisher).publishProductCreated(any(Product.class));

            Product savedProduct = productCaptor.getValue();
            assertEquals(imageUrls, savedProduct.getImageUrls());
        }

        @Test
        @DisplayName("Create Product - Missing Required Fields")
        void createProduct_MissingRequiredFields_ShouldThrowException() {
            // Title missing
            Product invalidProduct = validProduct.toBuilder().title("").build();
            Product finalInvalidProduct2 = invalidProduct;
            assertThrows(CustomException.class,
                    () -> productService.createProduct(finalInvalidProduct2, null));

            // Description missing
            invalidProduct = validProduct.toBuilder().description("").build();
            Product finalInvalidProduct1 = invalidProduct;
            assertThrows(CustomException.class,
                    () -> productService.createProduct(finalInvalidProduct1, null));

            // Category missing
            invalidProduct = validProduct.toBuilder().category("").build();
            Product finalInvalidProduct = invalidProduct;
            assertThrows(CustomException.class,
                    () -> productService.createProduct(finalInvalidProduct, null));
        }

        @Test
        @DisplayName("Create Product - Invalid Price")
        void createProduct_InvalidPrice_ShouldThrowException() {
            // Zero price
            Product invalidProduct = validProduct.toBuilder()
                    .price(Price.builder().amount(0.0).unit(CurrencyUnit.TOMAN).build())
                    .build();
            Product finalInvalidProduct = invalidProduct;
            assertThrows(CustomException.class,
                    () -> productService.createProduct(finalInvalidProduct, null));

            // Negative price
            invalidProduct = validProduct.toBuilder()
                    .price(Price.builder().amount(-100.0).unit(CurrencyUnit.TOMAN).build())
                    .build();
            Product finalInvalidProduct1 = invalidProduct;
            assertThrows(CustomException.class,
                    () -> productService.createProduct(finalInvalidProduct1, null));
        }
    }

    @Nested
    @DisplayName("Product Update Tests")
    class ProductUpdateTests {
        @Test
        @DisplayName("Update Product Successfully")
        void updateProduct_ValidUpdate_ShouldSucceed() {
            // Arrange
            when(productRepository.findById(1L)).thenReturn(Optional.of(validProduct));
            when(productRepository.save(any(Product.class))).thenReturn(validProduct);

            Product updateData = validProduct.toBuilder()
                    .title("Updated Title")
                    .description("Updated Description")
                    .price(Price.builder().amount(2000.0).unit(CurrencyUnit.TOMAN).build())
                    .build();

            // Act
            Product result = productService.updateProduct(1L, updateData, null);

            // Assert
            assertNotNull(result);
            verify(productRepository).save(productCaptor.capture());
            Product savedProduct = productCaptor.getValue();
            assertEquals("Updated Title", savedProduct.getTitle());
            assertEquals("Updated Description", savedProduct.getDescription());
            assertEquals(2000.0, savedProduct.getPrice().getAmount());
        }

        @Test
        @DisplayName("Update Product - Not Found")
        void updateProduct_NotFound_ShouldThrowException() {
            // Arrange
            when(productRepository.findById(anyLong())).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(CustomException.class,
                    () -> productService.updateProduct(1L, validProduct, null));
        }

        @Test
        @DisplayName("Update Product - Unauthorized Seller")
        void updateProduct_UnauthorizedSeller_ShouldThrowException() {
            // Arrange
            Product existingProduct = validProduct.toBuilder().sellerId(100L).build();
            Product updateProduct = validProduct.toBuilder().sellerId(200L).build();
            when(productRepository.findById(1L)).thenReturn(Optional.of(existingProduct));

            // Act & Assert
            assertThrows(CustomException.class,
                    () -> productService.updateProduct(1L, updateProduct, null));
        }
    }

    @Nested
    @DisplayName("Product Status Tests")
    class ProductStatusTests {
        @Test
        @DisplayName("Update Product Status Successfully")
        void updateProductStatus_ValidUpdate_ShouldSucceed() {
            // Arrange
            when(productRepository.findById(1L)).thenReturn(Optional.of(validProduct));
            when(productRepository.save(any(Product.class))).thenReturn(validProduct);

            // Act
            Product result = productService.updateProductStatus(1L, ProductStatus.RESERVED, 100L);

            // Assert
            assertNotNull(result);
            assertEquals(ProductStatus.RESERVED, result.getStatus());
            verify(eventPublisher).publishProductUpdated(any(Product.class));
        }

        @Test
        @DisplayName("Update Status of Deleted Product")
        void updateProductStatus_DeletedProduct_ShouldThrowException() {
            // Arrange
            Product deletedProduct = validProduct.toBuilder()
                    .status(ProductStatus.DELETED)
                    .build();
            when(productRepository.findById(1L)).thenReturn(Optional.of(deletedProduct));

            // Act & Assert
            assertThrows(CustomException.class,
                    () -> productService.updateProductStatus(1L, ProductStatus.ACTIVE, 100L));
        }
    }

    @Nested
    @DisplayName("Product Retrieval Tests")
    class ProductRetrievalTests {
        @Test
        @DisplayName("Get Product by ID Successfully")
        void getProduct_ExistingId_ShouldReturnProduct() {
            // Arrange
            when(productRepository.findById(1L)).thenReturn(Optional.of(validProduct));

            // Act
            Product result = productService.getProduct(1L);

            // Assert
            assertNotNull(result);
            assertEquals(validProduct.getId(), result.getId());
        }

        @Test
        @DisplayName("Get Product by ID - Not Found")
        void getProduct_NonexistentId_ShouldThrowException() {
            // Arrange
            when(productRepository.findById(anyLong())).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(CustomException.class,
                    () -> productService.getProduct(1L));
        }

        @Test
        @DisplayName("Get Products by Category")
        void getProductsByCategory_ShouldReturnFilteredList() {
            // Arrange
            List<Product> categoryProducts = Arrays.asList(validProduct, validProduct);
            when(productRepository.findByCategory("Electronics"))
                    .thenReturn(categoryProducts);

            // Act
            List<Product> results = productService.getProductsByCategory("Electronics");

            // Assert
            assertNotNull(results);
            assertEquals(2, results.size());
            assertTrue(results.stream()
                    .allMatch(p -> "Electronics".equals(p.getCategory())));
        }

        @Test
        @DisplayName("Get Products by Seller")
        void getProductsBySeller_ShouldReturnFilteredList() {
            // Arrange
            List<Product> sellerProducts = Arrays.asList(validProduct, validProduct);
            when(productRepository.findBySellerId(100L))
                    .thenReturn(sellerProducts);

            // Act
            List<Product> results = productService.getProductsBySeller(100L);

            // Assert
            assertNotNull(results);
            assertEquals(2, results.size());
            assertTrue(results.stream()
                    .allMatch(p -> Long.valueOf(100L).equals(p.getSellerId())));
        }
    }
}