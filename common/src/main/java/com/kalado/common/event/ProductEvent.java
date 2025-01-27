package com.kalado.common.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.kalado.common.dto.ProductDto;
import lombok.Data;

@Data
public class ProductEvent {
    private final String eventType;
    private final ProductDto product;

    @JsonCreator
    public ProductEvent(
            @JsonProperty("eventType") String eventType,
            @JsonProperty("product") ProductDto product) {
        this.eventType = eventType;
        this.product = product;
    }
}