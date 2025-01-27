package com.kalado.product.application.service;

import com.kalado.common.dto.AuthDto;
import com.kalado.common.enums.ErrorCode;
import com.kalado.common.enums.Role;
import com.kalado.common.exception.CustomException;
import com.kalado.common.feign.authentication.AuthenticationApi;
import com.kalado.product.domain.model.Product;
import com.kalado.common.enums.ProductStatus;
import com.kalado.product.infrastructure.messaging.ProductEventPublisher;
import com.kalado.product.infrastructure.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {
  private final AuthenticationApi authenticationApi;
  private final ProductRepository productRepository;
  private final ImageService imageService;
  private final ProductEventPublisher eventPublisher;


  private static final int MAX_IMAGES = 3;
  private static final long MAX_IMAGE_SIZE = 1024 * 1024;

  @Transactional
  public Product createProduct(Product product, List<MultipartFile> images) {
    validateProduct(product);

    if (images != null && !images.isEmpty()) {
      try {
        List<String> imageUrls = imageService.storeImages(images);
        product.setImageUrls(imageUrls);
        log.debug("Stored {} images for product. URLs: {}", images.size(), imageUrls);
      } catch (Exception e) {
        log.error("Failed to store images", e);
        throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR,
                "Failed to store images: " + e.getMessage());
      }
    } else {
      product.setImageUrls(new ArrayList<>());
    }

    product.setStatus(ProductStatus.ACTIVE);

    Product savedProduct = productRepository.save(product);
    eventPublisher.publishProductCreated(savedProduct);
    log.debug("Created product with ID: {} and {} images",
            savedProduct.getId(),
            savedProduct.getImageUrls().size());

    return savedProduct;
  }


  @Transactional
  public Product updateProduct(Long id, Product updatedProduct, List<MultipartFile> newImages) {
    Product existingProduct = getProduct(id);
    validateProductOwnership(existingProduct, updatedProduct.getSellerId());

    updateProductFields(existingProduct, updatedProduct);
    Product savedProduct = productRepository.save(existingProduct);

    eventPublisher.publishProductUpdated(savedProduct);

    return savedProduct;
  }

  @Transactional
  public void deleteProduct(Long id, Long sellerId) {
    Product product = getProduct(id);
    validateProductOwnership(product, sellerId);

    product.setStatus(ProductStatus.DELETED);
    Product deletedProduct = productRepository.save(product);

    eventPublisher.publishProductDeleted(deletedProduct);
  }

  @Transactional
  public Product updateProductStatus(Long id, ProductStatus newStatus, Long sellerId) {
    Product product = getProduct(id);

    if (!sellerId.equals(product.getSellerId()) && !isAdmin(sellerId)) {
      throw new CustomException(
              ErrorCode.FORBIDDEN,
              "You don't have permission to modify this product"
      );
    }

    if (newStatus == ProductStatus.DELETED) {
      product.setStatus(ProductStatus.DELETED);
      Product blockedProduct = productRepository.save(product);

      eventPublisher.publishProductDeleted(blockedProduct);

      log.info("Product blocked/deleted: {}", id);
      return blockedProduct;
    }

    if (product.getStatus() != ProductStatus.DELETED) {
      product.setStatus(newStatus);
      Product updatedProduct = productRepository.save(product);
      eventPublisher.publishProductUpdated(updatedProduct);
      log.info("Product status updated to {}: {}", newStatus, id);
      return updatedProduct;
    } else {
      throw new CustomException(
              ErrorCode.BAD_REQUEST,
              "Cannot update status of a blocked/deleted product"
      );
    }
  }

  private boolean isAdmin(Long userId) {
    try {
      AuthDto authDto = authenticationApi.validate(userId.toString());
      return authDto.getRole() == Role.ADMIN;
    } catch (Exception e) {
      log.error("Error validating admin status: {}", e.getMessage());
      return false;
    }
  }

  public Product getProduct(Long id) {
    return productRepository
        .findById(id)
        .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "Product not found"));
  }

  @Transactional(readOnly = true)
  public List<Product> getProductsBySeller(Long sellerId) {
    return productRepository.findBySellerId(sellerId);
  }

  @Transactional(readOnly = true)
  public List<Product> getProductsByCategory(String category) {
    return productRepository.findByCategory(category).stream()
            .collect(Collectors.toList());
  }

  private void validateProduct(Product product) {
    if (product.getTitle() == null || product.getTitle().trim().isEmpty()) {
      throw new CustomException(ErrorCode.BAD_REQUEST, "Title is required");
    }
    if (product.getDescription() == null || product.getDescription().trim().isEmpty()) {
      throw new CustomException(ErrorCode.BAD_REQUEST, "Description is required");
    }
    if (product.getPrice() == null || product.getPrice().getAmount() <= 0) {
      throw new CustomException(ErrorCode.BAD_REQUEST, "Valid price is required");
    }
    if (product.getCategory() == null || product.getCategory().trim().isEmpty()) {
      throw new CustomException(ErrorCode.BAD_REQUEST, "Category is required");
    }
  }

  private void validateImages(List<MultipartFile> images) {
    if (images == null) return;

    if (images.size() > MAX_IMAGES) {
      throw new CustomException(ErrorCode.BAD_REQUEST, "Maximum " + MAX_IMAGES + " images allowed");
    }

    for (MultipartFile image : images) {
      if (image.getSize() > MAX_IMAGE_SIZE) {
        throw new CustomException(ErrorCode.BAD_REQUEST, "Image size must be less than 1MB");
      }
      if (!Objects.requireNonNull(image.getContentType()).startsWith("image/")) {
        throw new CustomException(ErrorCode.BAD_REQUEST, "File must be an image");
      }
    }
  }

  private void validateProductOwnership(Product product, Long sellerId) {
    if (!product.getSellerId().equals(sellerId)) {
      throw new CustomException(
          ErrorCode.FORBIDDEN, "You don't have permission to modify this product");
    }
  }

  private void updateProductFields(Product existingProduct, Product updatedProduct) {
    existingProduct.setTitle(updatedProduct.getTitle());
    existingProduct.setDescription(updatedProduct.getDescription());
    existingProduct.setPrice(updatedProduct.getPrice());
    existingProduct.setCategory(updatedProduct.getCategory());
    existingProduct.setProductionYear(updatedProduct.getProductionYear());
    existingProduct.setBrand(updatedProduct.getBrand());
  }

  @Transactional(readOnly = true)
  public List<Product> getAllProducts() {
    return productRepository.findAll().stream()
            .collect(Collectors.toList());
  }
}
