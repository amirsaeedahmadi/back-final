package com.kalado.common.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

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
  private Map<String, String> details;
}