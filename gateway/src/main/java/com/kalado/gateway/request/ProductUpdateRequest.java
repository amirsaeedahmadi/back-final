package com.kalado.gateway.request;

import com.kalado.common.Price;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductUpdateRequest {
  private String title;
  private String description;
  private Price price;
  private String category;
  private Integer productionYear;
  private String brand;
}
