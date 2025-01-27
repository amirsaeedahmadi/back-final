package com.kalado.product.adapters.dto;

import com.kalado.common.Price;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductUpdateRequestDto {
    private String title;
    private String description;
    private Price price;
    private String category;
    private Integer productionYear;
    private String brand;
}
