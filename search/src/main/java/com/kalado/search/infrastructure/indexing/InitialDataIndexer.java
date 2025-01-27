package com.kalado.search.infrastructure.indexing;

import com.kalado.common.dto.ProductDto;
import com.kalado.common.feign.product.ProductApi;
import com.kalado.search.application.service.SearchService;
import com.kalado.search.domain.model.ProductDocument;
import com.kalado.search.domain.model.mapper.ProductSearchMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class InitialDataIndexer {

    private final ProductApi productApi;
    private final SearchService searchService;
    private final ProductSearchMapper productSearchMapper;

    @EventListener(ApplicationReadyEvent.class)
    public void indexExistingProducts() {
        try {
            log.info("Starting initial product indexing...");

            List<ProductDto> products = productApi.getAllProducts();

            for (ProductDto product : products) {
                ProductDocument document = productSearchMapper.dtoToDocument(product);
                searchService.indexProduct(document);
            }

            log.info("Completed initial indexing of {} products", products.size());
        } catch (Exception e) {
            log.error("Error during initial product indexing: {}", e.getMessage(), e);
        }
    }
}