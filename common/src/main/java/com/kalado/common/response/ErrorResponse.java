package com.kalado.common.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ErrorResponse {
  private Integer errorCode;
  private String message;
  private String timestamp;
  private Integer status;
  private String error;
  private String path;

  public ErrorResponse(Integer errorCode, String message) {
    this.errorCode = errorCode;
    this.message = message;
  }
}