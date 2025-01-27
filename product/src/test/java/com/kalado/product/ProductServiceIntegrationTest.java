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
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.mock.web.MockMultipartFile;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.*;
//
//@SpringBootTest
//class ProductServiceIntegrationTest {
//
//  @Autowired private ProductService productService;
//
//  @Autowired private ProductRepository productRepository;
//
//  @MockBean private ImageService imageService;
//
//  private static final Long TEST_USER_ID = 1L;
//
//  private Product testProduct;
//  private MockMultipartFile testImage;
//
//  @BeforeEach
//  void setUp() {
//    // Clear database
//    productRepository.deleteAll();
//
//    // Set up test image
//    //        testImage = new MockMultipartFile(
//    //                "image.jpg",
//    //                "image.jpg",
//    //                "image/jpeg",
//    //                "test image content".getBytes()
//    //        );
//
//    // Set up test product
//    testProduct =
//        Product.builder()
//            .title("Test Product")
//            .description("Test Description")
//            .price(new Price(100000, CurrencyUnit.TOMAN))
//            .category("Electronics")
//            .productionYear(2023)
//            .brand("Test Brand")
//            .sellerId(1L)
//            .build();
//
//    // Mock image service
//    //        when(imageService.storeImage(any())).thenReturn("test-image-url.jpg");
//  }
//
//  @Test
//  void createProduct_WithValidData_ShouldSucceed() {
//    Product product =
//        Product.builder()
//            .title("Test Product")
//            .description("Test Description")
//            .price(new Price(100.0, CurrencyUnit.TOMAN))
//            .category("Electronics")
//            .sellerId(1L)
//            .build();
//
//    Product savedProduct = productService.createProduct(product, null);
//
//    assertNotNull(savedProduct.getId());
//    assertEquals("Test Product", savedProduct.getTitle());
//  }
//
//  @Test
//  void createProduct_WithInvalidData_ShouldThrowException() {
//    Product invalidProduct = Product.builder().sellerId(1L).build();
//
//    assertThrows(
//        CustomException.class,
//        () -> {
//          productService.createProduct(invalidProduct, null);
//        });
//  }
//
//  @Test
//  void createProduct_WithNullPrice_ShouldThrowException() {
//    Product invalidProduct =
//        Product.builder()
//            .title("Test Product")
//            .description("Test Description")
//            .category("Electronics")
//            .sellerId(1L)
//            .build();
//
//    assertThrows(
//        CustomException.class,
//        () -> {
//          productService.createProduct(invalidProduct, null);
//        });
//  }
//
//  @Test
//  void createProduct_WithInvalidPrice_ShouldThrowException() {
//    Product invalidProduct =
//        Product.builder()
//            .title("Test Product")
//            .description("Test Description")
//            .price(new Price(-100.0, CurrencyUnit.TOMAN))
//            .category("Electronics")
//            .sellerId(1L)
//            .build();
//
//    assertThrows(
//        CustomException.class,
//        () -> {
//          productService.createProduct(invalidProduct, null);
//        });
//  }
//
//  @Test
//  void updateProduct_Success() {
//    // First create a product
//    Product savedProduct = productService.createProduct(testProduct, null);
//
//    // Update product
//    savedProduct.setTitle("Updated Title");
//    savedProduct.setDescription("Updated Description");
//
//    Product updatedProduct =
//        productService.updateProduct(
//            savedProduct.getId(), savedProduct, null // No new images
//            );
//
//    assertEquals("Updated Title", updatedProduct.getTitle());
//    assertEquals("Updated Description", updatedProduct.getDescription());
//    //        assertEquals(
//    //                new ArrayList<>(savedProduct.getImageUrls()),
//    //                new ArrayList<>(updatedProduct.getImageUrls()),
//    //                "Image URLs should match"
//    //        );
//  }
//
//  @Test
//  void deleteProduct_Success() {
//    // Create and save a product first
//    Product savedProduct = productService.createProduct(testProduct, null);
//    assertNotNull(savedProduct.getId());
//    assertEquals(ProductStatus.ACTIVE, savedProduct.getStatus());
//
//    // Delete the product
//    productService.deleteProduct(savedProduct.getId(), savedProduct.getSellerId());
//
//    // Verify the product's status is changed to DELETED
//    Product deletedProduct = productRepository.findById(savedProduct.getId()).orElseThrow();
//    assertEquals(ProductStatus.DELETED, deletedProduct.getStatus());
//  }
//
//  @Test
//  void deleteProduct_UnauthorizedUser_ShouldThrowException() {
//    // Create and save a product first
//    Product savedProduct = productService.createProduct(testProduct, null);
//    assertNotNull(savedProduct.getId());
//
//    // Try to delete with wrong seller ID
//    Long wrongSellerId = savedProduct.getSellerId() + 1;
//    CustomException exception =
//        assertThrows(
//            CustomException.class,
//            () -> {
//              productService.deleteProduct(savedProduct.getId(), wrongSellerId);
//            });
//
//    assertEquals(ErrorCode.FORBIDDEN, exception.getErrorCode());
//    assertEquals("You don't have permission to modify this product", exception.getMessage());
//
//    // Verify the product status hasn't changed
//    Product unchangedProduct = productRepository.findById(savedProduct.getId()).orElseThrow();
//    assertEquals(ProductStatus.ACTIVE, unchangedProduct.getStatus());
//  }
//
//  @Test
//  void deleteProduct_NonexistentProduct_ShouldThrowException() {
//    Long nonexistentId = 99999L;
//    CustomException exception =
//        assertThrows(
//            CustomException.class,
//            () -> {
//              productService.deleteProduct(nonexistentId, TEST_USER_ID);
//            });
//
//    assertEquals(ErrorCode.NOT_FOUND, exception.getErrorCode());
//    assertEquals("Product not found", exception.getMessage());
//  }
//
//  @Test
//  void updateProductStatus_Success() {
//    Product savedProduct = productService.createProduct(testProduct, null);
//
//    productService.updateProductStatus(
//        savedProduct.getId(), ProductStatus.RESERVED, savedProduct.getSellerId());
//
//    Product updatedProduct = productRepository.findById(savedProduct.getId()).orElseThrow();
//    assertEquals(ProductStatus.RESERVED, updatedProduct.getStatus());
//  }
//
//  @Test
//  void getProductsBySeller_Success() {
//    // Create first product
//    Product firstProduct =
//        Product.builder()
//            .title("First Product")
//            .description("Test Description 1")
//            .price(new Price(100000, CurrencyUnit.TOMAN))
//            .category("Electronics")
//            .productionYear(2023)
//            .brand("Test Brand 1")
//            .sellerId(1L)
//            .status(ProductStatus.ACTIVE)
//            .build();
//    productService.createProduct(firstProduct, null);
//
//    // Create second product
//    Product secondProduct =
//        Product.builder()
//            .title("Second Product")
//            .description("Test Description 2")
//            .price(new Price(200000, CurrencyUnit.TOMAN))
//            .category("Electronics")
//            .productionYear(2022)
//            .brand("Test Brand 2")
//            .sellerId(1L)
//            .status(ProductStatus.ACTIVE)
//            .build();
//    productService.createProduct(secondProduct, null);
//
//    // Create product for different seller
//    Product differentSellerProduct =
//        Product.builder()
//            .title("Different Seller Product")
//            .description("Test Description 3")
//            .price(new Price(150000, CurrencyUnit.TOMAN))
//            .category("Electronics")
//            .productionYear(2023)
//            .brand("Test Brand 3")
//            .sellerId(2L)
//            .status(ProductStatus.ACTIVE)
//            .build();
//    productService.createProduct(differentSellerProduct, null);
//
//    // Retrieve products for seller 1
//    List<Product> sellerProducts = productService.getProductsBySeller(1L);
//
//    // Assertions
//    assertEquals(2, sellerProducts.size(), "Should retrieve exactly 2 products for seller 1");
//    assertTrue(
//        sellerProducts.stream().allMatch(p -> p.getSellerId().equals(1L)),
//        "All retrieved products should belong to seller 1");
//    assertTrue(
//        sellerProducts.stream().anyMatch(p -> p.getTitle().equals("First Product")),
//        "Should contain first product");
//    assertTrue(
//        sellerProducts.stream().anyMatch(p -> p.getTitle().equals("Second Product")),
//        "Should contain second product");
//  }
//
//  @Test
//  void getProductsByCategory_Success() {
//    // Create electronics product
//    Product electronicsProduct =
//        Product.builder()
//            .title("Electronics Product")
//            .description("Test Description")
//            .price(new Price(100000, CurrencyUnit.TOMAN))
//            .category("Electronics")
//            .productionYear(2023)
//            .brand("Test Brand")
//            .sellerId(1L)
//            .status(ProductStatus.ACTIVE)
//            .build();
//    productService.createProduct(electronicsProduct, null);
//
//    // Create clothing product
//    Product clothingProduct =
//        Product.builder()
//            .title("Clothing Product")
//            .description("Test Description")
//            .price(new Price(50000, CurrencyUnit.TOMAN))
//            .category("Clothing")
//            .productionYear(2022)
//            .brand("Another Brand")
//            .sellerId(1L)
//            .status(ProductStatus.ACTIVE)
//            .build();
//    productService.createProduct(clothingProduct, null);
//
//    // Test retrieving products by category
//    List<Product> electronicsProducts = productService.getProductsByCategory("Electronics");
//    List<Product> clothingProducts = productService.getProductsByCategory("Clothing");
//
//    // Assertions
//    assertEquals(1, electronicsProducts.size(), "Should have one electronics product");
//    assertEquals(1, clothingProducts.size(), "Should have one clothing product");
//    assertEquals("Electronics", electronicsProducts.get(0).getCategory());
//    assertEquals("Clothing", clothingProducts.get(0).getCategory());
//    assertEquals("Electronics Product", electronicsProducts.get(0).getTitle());
//    assertEquals("Clothing Product", clothingProducts.get(0).getTitle());
//  }
//}
