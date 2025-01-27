package com.kalado.common.enums;

import com.kalado.common.Price;

import java.util.UUID;

public class TransactionDto {
  private UUID id;
  private TransactionStatus status;
  private Price price;
  private String orderID;
}
