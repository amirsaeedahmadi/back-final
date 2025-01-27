package com.kalado.search.infrastructure.messaging;

import com.kalado.common.event.ProductEvent;
import com.kalado.search.application.service.SearchService;
import com.kalado.search.domain.model.ProductDocument;
import com.kalado.search.domain.model.mapper.ProductSearchMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductEventConsumer {

    private final SearchService searchService;
    private final ProductSearchMapper productSearchMapper;

    @KafkaListener(topics = "product-events",
            groupId = "search-service",
            containerFactory = "kafkaListenerContainerFactory")
    public void handleProductEvent(ProductEvent event) {
        log.info("Received product event: {}", event);

        try {
            switch (event.getEventType()) {
                case "CREATED", "UPDATED" -> {
                    ProductDocument document = productSearchMapper.dtoToDocument(event.getProduct());
                    searchService.indexProduct(document);
                    log.info("Indexed product: {}", document.getId());
                }
                case "DELETED" -> {
                    searchService.deleteProduct(String.valueOf(event.getProduct().getId()));
                    log.info("Deleted product from index: {}", event.getProduct().getId());
                }
                default -> log.warn("Unknown event type: {}", event.getEventType());
            }
        } catch (Exception e) {
            log.error("Error processing product event: {}", e.getMessage(), e);
            throw e;
        }
    }
}