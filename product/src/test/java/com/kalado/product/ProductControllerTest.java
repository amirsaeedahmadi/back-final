//package com.kalado.product;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.kalado.common.Price;
//import com.kalado.common.dto.ProductDto;
//import com.kalado.common.dto.ProductStatusUpdateDto;
//import com.kalado.common.enums.CurrencyUnit;
//import com.kalado.common.enums.ErrorCode;
//import com.kalado.common.exception.CustomException;
//import com.kalado.product.adapters.controller.ProductMapper;
//import com.kalado.product.application.service.ProductService;
//import com.kalado.product.domain.model.Product;
//import com.kalado.common.enums.ProductStatus;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.http.MediaType;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
//
//import java.util.Arrays;
//import java.util.Collections;
//import java.util.List;
//
//import static org.hamcrest.Matchers.hasSize;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.eq;
//import static org.mockito.Mockito.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
//@SpringBootTest
//@AutoConfigureMockMvc
//class ProductControllerTest {
//
//  @Autowired private MockMvc mockMvc;
//
//  @Autowired private ObjectMapper objectMapper;
//
//  @MockBean private ProductService productService;
//
//  @MockBean private ProductMapper productMapper;
//
//  private ProductDto validProductDto;
//  private Product validProduct;
//
//  private ProductDto productDto;
//  private Product testProduct;
//  private final Long TEST_USER_ID = 1L;
//
//  @BeforeEach
//  void setUp() {
//    productDto =
//        ProductDto.builder()
//            .title("Test Product")
//            .description("Test Description")
//            .price(new Price(100000, CurrencyUnit.TOMAN))
//            .category("Electronics")
//            .productionYear(2023)
//            .brand("Test Brand")
//            .sellerId(TEST_USER_ID)
//            .build();
//
//    testProduct =
//        Product.builder()
//            .id(1L)
//            .title(productDto.getTitle())
//            .description(productDto.getDescription())
//            .price(productDto.getPrice())
//            .category(productDto.getCategory())
//            .productionYear(productDto.getProductionYear())
//            .brand(productDto.getBrand())
//            .sellerId(TEST_USER_ID)
//            .status(ProductStatus.ACTIVE)
//            .build();
//
//    validProductDto =
//        ProductDto.builder()
//            .title("Test Product")
//            .description("Test Description")
//            .price(new Price(100000, CurrencyUnit.TOMAN))
//            .category("Electronics")
//            .productionYear(2023)
//            .brand("Test Brand")
//            .sellerId(1L)
//            .build();
//
//    validProduct =
//        Product.builder()
//            .id(1L)
//            .title("Test Product")
//            .description("Test Description")
//            .price(new Price(100000, CurrencyUnit.TOMAN))
//            .category("Electronics")
//            .productionYear(2023)
//            .brand("Test Brand")
//            .sellerId(1L)
//            .build();
//  }
//
//  @Test
//  void createProduct_WithValidData_ShouldSucceed() throws Exception {
//    when(productMapper.toProduct(any(ProductDto.class))).thenReturn(validProduct);
//    when(productService.createProduct(any(Product.class), any())).thenReturn(validProduct);
//    when(productMapper.toResponseDto(any(Product.class))).thenReturn(validProductDto);
//
//    mockMvc
//        .perform(
//            MockMvcRequestBuilders.post("/products")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(objectMapper.writeValueAsString(validProductDto)))
//        .andExpect(status().isOk())
//        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//        .andExpect(jsonPath("$.title").value(validProductDto.getTitle()));
//  }
//
//  @Test
//  void createProduct_WithInvalidData_ShouldReturnBadRequest() throws Exception {
//    ProductDto invalidDto = ProductDto.builder().build();
//    when(productMapper.toProduct(any(ProductDto.class))).thenReturn(Product.builder().build());
//    when(productService.createProduct(any(Product.class), any()))
//        .thenThrow(new CustomException(ErrorCode.BAD_REQUEST, "Invalid product data"));
//
//    mockMvc
//        .perform(
//            MockMvcRequestBuilders.post("/products")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(objectMapper.writeValueAsString(invalidDto)))
//        .andExpect(status().isBadRequest());
//  }
//
//  @Test
//  void createProduct_WithNullPrice_ShouldReturnBadRequest() throws Exception {
//    ProductDto invalidDto =
//        ProductDto.builder()
//            .title("Test Product")
//            .description("Test Description")
//            .category("Electronics")
//            .sellerId(1L)
//            .build();
//
//    when(productMapper.toProduct(any(ProductDto.class)))
//        .thenReturn(
//            Product.builder()
//                .title("Test Product")
//                .description("Test Description")
//                .category("Electronics")
//                .sellerId(1L)
//                .build());
//
//    when(productService.createProduct(any(Product.class), any()))
//        .thenThrow(new CustomException(ErrorCode.BAD_REQUEST, "Price is required"));
//
//    mockMvc
//        .perform(
//            MockMvcRequestBuilders.post("/products")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(objectMapper.writeValueAsString(invalidDto)))
//        .andExpect(status().isBadRequest());
//  }
//
//  @Test
//  void updateProduct_Success() throws Exception {
//    when(productMapper.toProduct(any(ProductDto.class))).thenReturn(testProduct);
//    when(productService.updateProduct(eq(1L), any(Product.class), eq(null)))
//        .thenReturn(testProduct);
//    when(productMapper.toResponseDto(any(Product.class))).thenReturn(productDto);
//
//    mockMvc
//        .perform(
//            MockMvcRequestBuilders.put("/products/{id}", 1L)
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(objectMapper.writeValueAsString(productDto)))
//        .andExpect(status().isOk())
//        .andExpect(jsonPath("$.title").value(productDto.getTitle()));
//
//    verify(productService).updateProduct(eq(1L), any(Product.class), eq(null));
//  }
//
//  @Test
//  void deleteProduct_Success() throws Exception {
//    doNothing().when(productService).deleteProduct(1L, TEST_USER_ID);
//
//    mockMvc.perform(MockMvcRequestBuilders.put("/products/delete/{id}", 1L)
//                    .param("userId", String.valueOf(TEST_USER_ID))
//                    .contentType(MediaType.APPLICATION_JSON))
//            .andExpect(status().isOk());
//
//    verify(productService).deleteProduct(1L, TEST_USER_ID);
//  }
//
//
//  @Test
//  void deleteProduct_UnauthorizedUser() throws Exception {
//    Long unauthorizedUserId = 999L;
//    doThrow(new CustomException(ErrorCode.FORBIDDEN,
//            "You don't have permission to modify this product"))
//            .when(productService).deleteProduct(1L, unauthorizedUserId);
//
//    mockMvc.perform(MockMvcRequestBuilders.put("/products/delete/{id}", 1L)
//                    .param("userId", String.valueOf(unauthorizedUserId))
//                    .contentType(MediaType.APPLICATION_JSON))
//            .andExpect(status().isForbidden())
//            .andExpect(jsonPath("$.errorCode").value(ErrorCode.FORBIDDEN.getErrorCodeValue()))
//            .andExpect(jsonPath("$.message")
//                    .value("You don't have permission to modify this product"));
//
//    verify(productService).deleteProduct(1L, unauthorizedUserId);
//  }
//
//  @Test
//  void updateProductStatus_Success() throws Exception {
//    ProductStatusUpdateDto statusUpdate = new ProductStatusUpdateDto(ProductStatus.RESERVED);
//    testProduct.setStatus(ProductStatus.RESERVED);
//
//    when(productService.updateProductStatus(eq(1L), eq(ProductStatus.RESERVED), eq(TEST_USER_ID)))
//        .thenReturn(testProduct);
//    when(productMapper.toResponseDto(testProduct)).thenReturn(productDto);
//
//    mockMvc
//        .perform(
//            MockMvcRequestBuilders.put("/products/status/{id}", 1L)
//                .param("userId", TEST_USER_ID.toString())
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(objectMapper.writeValueAsString(statusUpdate)))
//        .andExpect(status().isOk())
//        .andExpect(content().contentType(MediaType.APPLICATION_JSON));
//  }
//
//  @Test
//  void getSellerProducts_Success() throws Exception {
//    List<Product> testProducts = Arrays.asList(testProduct);
//    when(productService.getProductsBySeller(TEST_USER_ID)).thenReturn(testProducts);
//    when(productMapper.toResponseDto(testProduct)).thenReturn(productDto);
//
//    mockMvc
//        .perform(MockMvcRequestBuilders.get("/products/seller/{sellerId}", TEST_USER_ID))
//        .andExpect(status().isOk())
//        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//        .andExpect(jsonPath("$", hasSize(1)))
//        .andExpect(jsonPath("$[0].title").value(productDto.getTitle()))
//        .andExpect(jsonPath("$[0].description").value(productDto.getDescription()))
//        .andExpect(jsonPath("$[0].category").value(productDto.getCategory()));
//
//    verify(productService).getProductsBySeller(TEST_USER_ID);
//    verify(productMapper).toResponseDto(testProduct);
//  }
//
//  @Test
//  void getProductsByCategory_Success() throws Exception {
//    List<Product> testProducts = Arrays.asList(testProduct);
//    String category = "Electronics";
//    when(productService.getProductsByCategory(category)).thenReturn(testProducts);
//    when(productMapper.toResponseDto(testProduct)).thenReturn(productDto);
//
//    mockMvc
//        .perform(MockMvcRequestBuilders.get("/products/category/{category}", category))
//        .andExpect(status().isOk())
//        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//        .andExpect(jsonPath("$", hasSize(1)))
//        .andExpect(jsonPath("$[0].title").value(productDto.getTitle()))
//        .andExpect(jsonPath("$[0].category").value(productDto.getCategory()));
//
//    verify(productService).getProductsByCategory(category);
//    verify(productMapper).toResponseDto(testProduct);
//  }
//
//  @Test
//  void getProduct_Success() throws Exception {
//    Long productId = 1L;
//    when(productService.getProduct(productId)).thenReturn(testProduct);
//    when(productMapper.toResponseDto(testProduct)).thenReturn(productDto);
//
//    mockMvc
//        .perform(MockMvcRequestBuilders.get("/products/{id}", productId))
//        .andExpect(status().isOk())
//        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//        .andExpect(jsonPath("$.title").value(productDto.getTitle()))
//        .andExpect(jsonPath("$.description").value(productDto.getDescription()))
//        .andExpect(jsonPath("$.category").value(productDto.getCategory()));
//
//    verify(productService).getProduct(productId);
//    verify(productMapper).toResponseDto(testProduct);
//  }
//
//  @Test
//  void getProduct_NotFound() throws Exception {
//    Long nonExistentId = 999L;
//    when(productService.getProduct(nonExistentId))
//        .thenThrow(new CustomException(ErrorCode.NOT_FOUND, "Product not found"));
//
//    mockMvc
//        .perform(MockMvcRequestBuilders.get("/products/{id}", nonExistentId))
//        .andExpect(status().isNotFound())
//        .andExpect(jsonPath("$.errorCode").value(ErrorCode.NOT_FOUND.getErrorCodeValue()));
//
//    verify(productService).getProduct(nonExistentId);
//  }
//
//  @Test
//  void updateProductStatus_WithInvalidStatus() throws Exception {
//    ProductStatusUpdateDto statusUpdate = new ProductStatusUpdateDto(null);
//
//    mockMvc
//        .perform(
//            MockMvcRequestBuilders.put("/products/status/{id}", 1L)
//                .param("userId", TEST_USER_ID.toString())
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(objectMapper.writeValueAsString(statusUpdate)))
//        .andExpect(status().isBadRequest())
//        .andExpect(jsonPath("$.errorCode").value(ErrorCode.BAD_REQUEST.getErrorCodeValue()))
//        .andExpect(jsonPath("$.message").value("Status cannot be null"));
//
//    verify(productService, never()).updateProductStatus(any(), any(), any());
//  }
//
//  @Test
//  void updateProductStatus_UnauthorizedUser() throws Exception {
//    ProductStatusUpdateDto statusUpdate = new ProductStatusUpdateDto(ProductStatus.RESERVED);
//
//    when(productService.updateProductStatus(eq(1L), eq(ProductStatus.RESERVED), eq(999L)))
//        .thenThrow(
//            new CustomException(
//                ErrorCode.FORBIDDEN, "You don't have permission to modify this product"));
//
//    mockMvc
//        .perform(
//            MockMvcRequestBuilders.put("/products/status/{id}", 1L)
//                .param("userId", "999")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(objectMapper.writeValueAsString(statusUpdate)))
//        .andExpect(status().isForbidden())
//        .andExpect(jsonPath("$.errorCode").value(ErrorCode.FORBIDDEN.getErrorCodeValue()))
//        .andExpect(jsonPath("$.message").value("You don't have permission to modify this product"));
//
//    verify(productService).updateProductStatus(1L, ProductStatus.RESERVED, 999L);
//  }
//}
