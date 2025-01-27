package com.kalado.common.feign.payment;

import com.kalado.common.dto.TransactionDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "payment-service", contextId = "payment-service", path = "/payment")
public interface PaymentApi {
  @PostMapping("/pay")
  boolean pay(@RequestParam String transactionID);

  @PostMapping("/createOrderTransaction")
  TransactionDto createOrderTransaction(@RequestParam Long shoppingCartId, @RequestParam double amount);
}
