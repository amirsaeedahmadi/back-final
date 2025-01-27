package com.kalado.payment.domain.mapper;

import com.kalado.common.dto.*;
import com.kalado.payment.domain.model.Transaction;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface TransactionMapper {
  TransactionMapper INSTANCE = Mappers.getMapper(TransactionMapper.class);

  TransactionDto transactionToDto(Transaction transaction);
}
