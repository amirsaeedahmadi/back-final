package com.kalado.gateway.exception;

import com.kalado.common.enums.ErrorCode;
import lombok.Getter;

@Getter
public class CustomGatewayException extends RuntimeException {
  private final ErrorCode errorCode;

  public CustomGatewayException(ErrorCode errorCode, String message) {
    super(message != null ? message : errorCode.getErrorMessageValue());
    this.errorCode = errorCode;
  }
}