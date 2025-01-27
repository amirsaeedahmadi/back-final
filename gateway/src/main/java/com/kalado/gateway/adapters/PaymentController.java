package com.kalado.gateway.adapters;

import com.kalado.common.dto.TransactionDto;
import com.kalado.common.feign.payment.PaymentApi;
import com.kalado.gateway.annotation.Authentication;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("v1/payment")
@RequiredArgsConstructor
public class PaymentController {
  private final PaymentApi paymentApi;

  @PostMapping("/pay")
  @Authentication
  public boolean pay(@RequestParam String transactionID) {
    return paymentApi.pay(transactionID);
  }

  @PostMapping("/createOrderTransaction")
  @Authentication
  public TransactionDto createOrderTransaction(@RequestParam Long shoppingCartId, @RequestParam double amount) {
    return paymentApi.createOrderTransaction(shoppingCartId, amount);
  }
}
