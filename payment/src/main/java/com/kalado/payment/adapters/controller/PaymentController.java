package com.kalado.payment.adapters.controller;

import com.kalado.common.Price;
import com.kalado.common.dto.TransactionDto;
import com.kalado.common.enums.CurrencyUnit;
import com.kalado.common.feign.payment.PaymentApi;
import com.kalado.payment.application.service.PaymentService;
import com.kalado.payment.domain.mapper.TransactionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
public class PaymentController implements PaymentApi {
  private final PaymentService paymentService;

  @Override
  public boolean pay(String transactionID) {
    return paymentService.pay(transactionID);
  }

  @Override
  public TransactionDto createOrderTransaction(Long shoppingCartId, double amount) {
    Price price = Price.builder().amount(amount).unit(CurrencyUnit.TOMAN).build();
    return TransactionMapper.INSTANCE.transactionToDto(
        paymentService.createOrderTransaction(shoppingCartId, price));
  }
}
