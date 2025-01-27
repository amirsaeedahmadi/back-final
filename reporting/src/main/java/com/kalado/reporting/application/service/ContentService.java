package com.kalado.reporting.application.service;

import com.kalado.common.dto.ProductDto;
import com.kalado.common.enums.ErrorCode;
import com.kalado.common.exception.CustomException;
import com.kalado.common.feign.product.ProductApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContentService {
    private final ProductApi productApi;

    public Long determineContentOwner(String contentType, Long contentId) {
        try {
            return switch (contentType.toUpperCase()) {
                case "PRODUCT" -> {
                    ProductDto product = productApi.getProduct(contentId);
                    if (product == null) {
                        throw new CustomException(ErrorCode.NOT_FOUND, "Product not found");
                    }
                    yield product.getSellerId();
                }
                default -> throw new CustomException(ErrorCode.BAD_REQUEST,
                        "Unsupported content type: " + contentType);
            };
        } catch (Exception e) {
            log.error("Error determining content owner for type: {} and id: {}", contentType, contentId, e);
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR,
                    "Failed to determine content owner: " + e.getMessage());
        }
    }
}