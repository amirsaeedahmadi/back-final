package com.kalado.product.infrastructure.messaging;

import com.kalado.common.event.ProductEvent;
import com.kalado.product.domain.model.Product;
import com.kalado.product.adapters.controller.ProductMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductEventPublisher {

    private final KafkaTemplate<String, ProductEvent> kafkaTemplate;
    private final ProductMapper productMapper;
    private static final String TOPIC = "product-events";

    public void publishProductCreated(Product product) {
        ProductEvent event = new ProductEvent("CREATED", productMapper.toResponseDto(product));
        kafkaTemplate.send(TOPIC, String.valueOf(product.getId()), event)
                .addCallback(
                        result -> log.info("Successfully published product created event for id: {}", product.getId()),
                        ex -> log.error("Failed to publish product created event for id: {}", product.getId(), ex)
                );
    }

    public void publishProductUpdated(Product product) {
        ProductEvent event = new ProductEvent("UPDATED", productMapper.toResponseDto(product));
        kafkaTemplate.send(TOPIC, String.valueOf(product.getId()), event)
                .addCallback(
                        result -> log.info("Successfully published product updated event for id: {}", product.getId()),
                        ex -> log.error("Failed to publish product updated event for id: {}", product.getId(), ex)
                );
    }

    public void publishProductDeleted(Product product) {
        ProductEvent event = new ProductEvent("DELETED", productMapper.toResponseDto(product));
        kafkaTemplate.send(TOPIC, String.valueOf(product.getId()), event)
                .addCallback(
                        result -> log.info("Successfully published product deleted event for id: {}", product.getId()),
                        ex -> log.error("Failed to publish product deleted event for id: {}", product.getId(), ex)
                );
    }
}