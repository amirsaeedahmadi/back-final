package com.kalado.common.dto;

import com.kalado.common.enums.ProductStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductStatusUpdateDto {
  private ProductStatus status;
}
