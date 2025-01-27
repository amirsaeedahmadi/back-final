package com.kalado.common;

import com.kalado.common.enums.CurrencyUnit;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.persistence.Embeddable;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Embeddable
@NoArgsConstructor
@Builder
public class Price {
  private double amount;
  private CurrencyUnit unit;

  @JsonCreator
  public Price(@JsonProperty("amount") double amount, @JsonProperty("unit") CurrencyUnit unit) {
    this.amount = amount;
    this.unit = (unit == null) ? CurrencyUnit.TOMAN : unit;
  }
}
