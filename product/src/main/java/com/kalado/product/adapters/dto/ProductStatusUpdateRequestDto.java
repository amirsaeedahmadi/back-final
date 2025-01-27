package com.kalado.product.adapters.dto;

import com.kalado.common.enums.ProductStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductStatusUpdateRequestDto {
    private ProductStatus status;
}
