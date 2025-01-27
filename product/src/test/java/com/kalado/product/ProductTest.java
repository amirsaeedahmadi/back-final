//package com.kalado.product;
//
//import com.kalado.common.Price;
//import com.kalado.common.enums.CurrencyUnit;
//import com.kalado.common.enums.ErrorCode;
//import com.kalado.common.exception.CustomException;
//import com.kalado.product.application.service.ImageService;
//import com.kalado.product.application.service.ProductService;
//import com.kalado.product.domain.model.Product;
//import com.kalado.common.enums.ProductStatus;
//import com.kalado.product.infrastructure.repository.ProductRepository;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.mock.web.MockMultipartFile;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.util.Arrays;
//import java.util.List;
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class ProductTest {
//
//    @Mock
//    private ProductRepository productRepository;
//
//    @Mock
//    private ImageService imageService;
//
//    @InjectMocks
//    private ProductService productService;
//
//    private Product validProduct;
//    private List<MultipartFile> validImages;
//
//    @BeforeEach
//    void setUp() {
//        validProduct = Product.builder()
//                .id(1L)
//                .title("Test Product")
//                .description("Test Description")
//                .price(new Price(100000, CurrencyUnit.TOMAN))
//                .category("Electronics")
//                .productionYear(2023)
//                .brand("Test Brand")
//                .sellerId(1L)
//                .status(ProductStatus.ACTIVE)
//                .build();
//
//        MockMultipartFile mockImage = new MockMultipartFile(
//                "image",
//                "test.jpg",
//                "image/jpeg",
//                "test image content".getBytes()
//        );
//        validImages = Arrays.asList(mockImage);
//    }
//
//    @Test
//    void createProduct_WithValidData_ShouldSucceed() {
//        when(productRepository.save(any(Product.class))).thenReturn(validProduct);
//
//        Product result = productService.createProduct(validProduct, null);
//
//        assertNotNull(result);
//        assertEquals(validProduct.getTitle(), result.getTitle());
//        assertEquals(ProductStatus.ACTIVE, result.getStatus());
//        verify(productRepository).save(any(Product.class));
//    }
//
//    @Test
//    void createProduct_WithNullTitle_ShouldThrowException() {
//        Product invalidProduct = validProduct.toBuilder().title(null).build();
//
//        CustomException exception = assertThrows(
//                CustomException.class,
//                () -> productService.createProduct(invalidProduct, null)
//        );
//
//        assertEquals(ErrorCode.BAD_REQUEST, exception.getErrorCode());
//        assertEquals("Title is required", exception.getMessage());
//    }
//
//    @Test
//    void createProduct_WithEmptyDescription_ShouldThrowException() {
//        Product invalidProduct = validProduct.toBuilder().description("").build();
//
//        CustomException exception = assertThrows(
//                CustomException.class,
//                () -> productService.createProduct(invalidProduct, null)
//        );
//
//        assertEquals(ErrorCode.BAD_REQUEST, exception.getErrorCode());
//        assertEquals("Description is required", exception.getMessage());
//    }
//
//    @Test
//    void createProduct_WithNegativePrice_ShouldThrowException() {
//        Product invalidProduct = validProduct.toBuilder()
//                .price(new Price(-100, CurrencyUnit.TOMAN))
//                .build();
//
//        CustomException exception = assertThrows(
//                CustomException.class,
//                () -> productService.createProduct(invalidProduct, null)
//        );
//
//        assertEquals(ErrorCode.BAD_REQUEST, exception.getErrorCode());
//        assertEquals("Valid price is required", exception.getMessage());
//    }
//
//    @Test
//    void updateProduct_WhenProductExists_ShouldSucceed() {
//        Product updatedProduct = validProduct.toBuilder()
//                .title("Updated Title")
//                .description("Updated Description")
//                .build();
//
//        when(productRepository.findById(1L)).thenReturn(Optional.of(validProduct));
//        when(productRepository.save(any(Product.class))).thenReturn(updatedProduct);
//
//        Product result = productService.updateProduct(1L, updatedProduct, null);
//
//        assertNotNull(result);
//        assertEquals("Updated Title", result.getTitle());
//        assertEquals("Updated Description", result.getDescription());
//        verify(productRepository).save(any(Product.class));
//    }
//
//    @Test
//    void updateProduct_WhenProductNotFound_ShouldThrowException() {
//        when(productRepository.findById(1L)).thenReturn(Optional.empty());
//
//        CustomException exception = assertThrows(
//                CustomException.class,
//                () -> productService.updateProduct(1L, validProduct, null)
//        );
//
//        assertEquals(ErrorCode.NOT_FOUND, exception.getErrorCode());
//        assertEquals("Product not found", exception.getMessage());
//    }
//
//    @Test
//    void updateProduct_WithUnauthorizedSeller_ShouldThrowException() {
//        Product productWithDifferentSeller = validProduct.toBuilder()
//                .sellerId(2L)
//                .build();
//
//        when(productRepository.findById(1L)).thenReturn(Optional.of(validProduct));
//
//        CustomException exception = assertThrows(
//                CustomException.class,
//                () -> productService.updateProduct(1L, productWithDifferentSeller, null)
//        );
//
//        assertEquals(ErrorCode.FORBIDDEN, exception.getErrorCode());
//    }
//
//    @Test
//    void updateProductStatus_WhenProductExists_ShouldSucceed() {
//        when(productRepository.findById(1L)).thenReturn(Optional.of(validProduct));
//        when(productRepository.save(any(Product.class))).thenAnswer(i -> i.getArgument(0));
//
//        Product result = productService.updateProductStatus(1L, ProductStatus.RESERVED, 1L);
//
//        assertNotNull(result);
//        assertEquals(ProductStatus.RESERVED, result.getStatus());
//        verify(productRepository).save(any(Product.class));
//    }
//
//    @Test
//    void getProduct_WhenProductExists_ShouldReturnProduct() {
//        when(productRepository.findById(1L)).thenReturn(Optional.of(validProduct));
//
//        Product result = productService.getProduct(1L);
//
//        assertNotNull(result);
//        assertEquals(validProduct.getId(), result.getId());
//        assertEquals(validProduct.getTitle(), result.getTitle());
//    }
//
//    @Test
//    void getProduct_WhenProductNotFound_ShouldThrowException() {
//        when(productRepository.findById(1L)).thenReturn(Optional.empty());
//
//        CustomException exception = assertThrows(
//                CustomException.class,
//                () -> productService.getProduct(1L)
//        );
//
//        assertEquals(ErrorCode.NOT_FOUND, exception.getErrorCode());
//        assertEquals("Product not found", exception.getMessage());
//    }
//
//    @Test
//    void deleteProduct_WhenProductExists_ShouldSetStatusToDeleted() {
//        when(productRepository.findById(1L)).thenReturn(Optional.of(validProduct));
//        when(productRepository.save(any(Product.class))).thenAnswer(i -> i.getArgument(0));
//
//        productService.deleteProduct(1L, 1L);
//
//        verify(productRepository).save(argThat(product ->
//                product.getStatus() == ProductStatus.DELETED
//        ));
//    }
//
//    @Test
//    void deleteProduct_WithUnauthorizedSeller_ShouldThrowException() {
//        when(productRepository.findById(1L)).thenReturn(Optional.of(validProduct));
//
//        CustomException exception = assertThrows(
//                CustomException.class,
//                () -> productService.deleteProduct(1L, 2L)
//        );
//
//        assertEquals(ErrorCode.FORBIDDEN, exception.getErrorCode());
//    }
//
//    @Test
//    void getProductsBySeller_ShouldReturnSellerProducts() {
//        List<Product> expectedProducts = Arrays.asList(validProduct);
//        when(productRepository.findBySellerId(1L)).thenReturn(expectedProducts);
//
//        List<Product> result = productService.getProductsBySeller(1L);
//
//        assertNotNull(result);
//        assertEquals(1, result.size());
//        assertEquals(validProduct.getId(), result.get(0).getId());
//    }
//
//    @Test
//    void getProductsByCategory_ShouldReturnCategoryProducts() {
//        List<Product> expectedProducts = Arrays.asList(validProduct);
//        when(productRepository.findByCategory("Electronics")).thenReturn(expectedProducts);
//
//        List<Product> result = productService.getProductsByCategory("Electronics");
//
//        assertNotNull(result);
//        assertEquals(1, result.size());
//        assertEquals(validProduct.getCategory(), result.get(0).getCategory());
//    }
//}