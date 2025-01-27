package com.kalado.common.feign.product;

import com.kalado.common.dto.ProductDto;
import com.kalado.common.dto.ProductStatusUpdateDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import org.springframework.core.io.Resource;


@FeignClient(name = "product-service", path = "/products")
public interface ProductApi {

    @GetMapping(value = "/images/{filename}", produces = MediaType.IMAGE_JPEG_VALUE)
    Resource getImage(@PathVariable String filename);

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ProductDto createProduct(
            @RequestPart(value = "product") String productJson,
            @RequestPart(value = "images", required = false) List<MultipartFile> images
    );

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ProductDto updateProduct(
            @PathVariable Long id,
            @RequestPart(value = "product") String productJson,
            @RequestPart(value = "images", required = false) List<MultipartFile> images
    );


    @PutMapping("/delete/{id}")
    void deleteProduct(
            @PathVariable Long id,
            @RequestParam("userId") Long sellerId
    );

    @PutMapping(value = "/status/{id}")
    ProductDto updateProductStatus(
            @PathVariable Long id,
            @RequestParam("userId") Long userId,
            @RequestBody ProductStatusUpdateDto statusUpdate
    );

    @GetMapping("/seller/{sellerId}")
    List<ProductDto> getSellerProducts(@PathVariable Long sellerId);

    @GetMapping("/category/{category}")
    List<ProductDto> getProductsByCategory(@PathVariable String category);

    @GetMapping("/{id}")
    ProductDto getProduct(@PathVariable Long id);

    @GetMapping("/all")
    List<ProductDto> getAllProducts();
}