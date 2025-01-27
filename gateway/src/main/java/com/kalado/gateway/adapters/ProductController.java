package com.kalado.gateway.adapters;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kalado.common.dto.ProductDto;
import com.kalado.common.dto.ProductStatusUpdateDto;
import com.kalado.common.enums.ErrorCode;
import com.kalado.common.exception.CustomException;
import com.kalado.common.feign.product.ProductApi;
import com.kalado.gateway.annotation.Authentication;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/v1/product")
@RequiredArgsConstructor
@Slf4j
public class ProductController {
  private final ProductApi productApi;
  private final ObjectMapper objectMapper;

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @Authentication(userId = "#userId")
  public ProductDto createProduct(
          Long userId,
          @RequestParam("product") String productJson,
          @RequestParam(value = "images", required = false) List<MultipartFile> images) {
    try {
      ProductDto productDto = objectMapper.readValue(productJson, ProductDto.class);

      productDto.setSellerId(userId);

      String updatedProductJson = objectMapper.writeValueAsString(productDto);

      log.debug("Creating product with data: {} and {} images",
              productDto,
              images != null ? images.size() : 0);

      return productApi.createProduct(updatedProductJson, images);
    } catch (JsonProcessingException e) {
      log.error("Error parsing product JSON: {}", e.getMessage());
      throw new CustomException(ErrorCode.BAD_REQUEST,
              "Invalid product data format: " + e.getMessage());
    } catch (Exception e) {
      log.error("Error creating product: {}", e.getMessage(), e);
      throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR,
              "Error processing product creation: " + e.getMessage());
    }
  }

  @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @Authentication(userId = "#userId")
  public ProductDto updateProduct(
          Long userId,
          @PathVariable Long id,
          @RequestParam("product") String productJson,
          @RequestParam(value = "images", required = false) List<MultipartFile> images) {
    try {
      ProductDto productDto = objectMapper.readValue(productJson, ProductDto.class);

      productDto.setSellerId(userId);

      productDto.setId(id);

      String updatedProductJson = objectMapper.writeValueAsString(productDto);

      log.debug("Updating product {} with data: {} and {} images",
              id,
              productDto,
              images != null ? images.size() : 0);

      return productApi.updateProduct(id, updatedProductJson, images);

    } catch (JsonProcessingException e) {
      log.error("Error parsing product JSON for update: {}", e.getMessage());
      throw new CustomException(
              ErrorCode.BAD_REQUEST,
              "Invalid product data format: " + e.getMessage()
      );

    } catch (CustomException e) {
      log.error("Business logic error during product update: {}", e.getMessage());
      throw e;

    } catch (Exception e) {
      log.error("Unexpected error updating product {}: {}", id, e.getMessage(), e);
      throw new CustomException(
              ErrorCode.INTERNAL_SERVER_ERROR,
              "Error processing product update: " + e.getMessage()
      );
    }
  }

  @PutMapping("/delete/{id}")
  @Authentication(userId = "#userId")
  public void deleteProduct(Long userId, @PathVariable Long id) {
    productApi.deleteProduct(id, userId);
  }

  @PutMapping(value = "/status/{id}")
  @Authentication(userId = "#userId")
  public ProductDto updateProductStatus(
          Long userId,
          @PathVariable Long id,
          @RequestBody ProductStatusUpdateDto statusUpdate) {
    if (statusUpdate == null || statusUpdate.getStatus() == null) {
      throw new CustomException(ErrorCode.BAD_REQUEST, "Status cannot be null");
    }
    return productApi.updateProductStatus(id, userId, statusUpdate);
  }

  @GetMapping("/seller")
  @Authentication(userId = "#userId")
  public List<ProductDto> getSellerProducts(Long userId) {
    return productApi.getSellerProducts(userId);
  }

  @GetMapping("/seller/{userId}")
  public List<ProductDto> getSellerProductsBySellerId(@PathVariable Long userId) {
    return productApi.getSellerProducts(userId);
  }

  @GetMapping("/category/{category}")
  public List<ProductDto> getProductsByCategory(@PathVariable String category) {
    return productApi.getProductsByCategory(category);
  }

  @GetMapping("/{id}")
  public ProductDto getProduct(@PathVariable Long id) {
    return productApi.getProduct(id);
  }
}