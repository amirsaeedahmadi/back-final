package com.kalado.payment.application.service;

import com.kalado.common.Price;
import com.kalado.common.enums.TransactionStatus;
import com.kalado.payment.domain.model.Payment;
import com.kalado.payment.domain.model.Transaction;
import com.kalado.payment.infrastructure.repository.PaymentRepository;
import com.kalado.payment.infrastructure.repository.TransactionRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentService {

  private final TransactionRepository transactionRepository;

  private final PaymentRepository paymentRepository;

  public boolean pay(String transactionId) {
    Optional<Transaction> transactionOptional =
        transactionRepository.findById(UUID.fromString(transactionId));
    if (transactionOptional.isPresent()) {
      transactionOptional.get().setStatus(TransactionStatus.PAID);
      transactionRepository.save(transactionOptional.get());
      Payment payment = Payment.builder().transactionId(transactionId).build();
      paymentRepository.save(payment);
      return true;
    }
    return false;
  }

  public Transaction createOrderTransaction(long orderId, Price price) {
    return transactionRepository.save(Transaction.builder().orderId(orderId).price(price).build());
  }
}
