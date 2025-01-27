package com.kalado.gateway.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kalado.common.enums.ErrorCode;
import com.kalado.common.response.ErrorResponse;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.IOException;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class ExceptionHandlerAdvice {
  private final ObjectMapper objectMapper;

  @ExceptionHandler(FeignException.Conflict.class)
  public ResponseEntity<ErrorResponse> handleFeignConflict(FeignException.Conflict ex) {
    log.debug("Handling Conflict exception: {}", ex.getMessage());
    return handleFeignException(ex, 409);
  }

  @ExceptionHandler({
          FeignException.BadRequest.class,
          FeignException.Unauthorized.class,
          FeignException.Forbidden.class,
          FeignException.NotFound.class
  })
  public ResponseEntity<ErrorResponse> handleClientError(FeignException e) {
    log.debug("Handling client error: {}", e.getMessage());
    return handleFeignException(e, e.status());
  }

  private ResponseEntity<ErrorResponse> handleFeignException(FeignException ex, int status) {
    ErrorResponse errorResponse;
    try {
      String responseBody = ex.contentUTF8();
      errorResponse = objectMapper.readValue(responseBody, ErrorResponse.class);
    } catch (IOException e) {
      log.warn("Could not parse error response, using default", e);
      errorResponse = new ErrorResponse(
              status,
              ex.getMessage()
      );
    }

    return ResponseEntity
            .status(status)
            .body(errorResponse);
  }

  @ExceptionHandler(CustomGatewayException.class)
  public ResponseEntity<ErrorResponse> handleCustomGatewayException(CustomGatewayException ex) {
    log.error("Handling CustomGatewayException: {}", ex.getMessage());

    ErrorResponse errorResponse = new ErrorResponse(
            ex.getErrorCode().getErrorCodeValue(),
            ex.getMessage()
    );

    return ResponseEntity
            .status(ex.getErrorCode().getHttpStatus())
            .body(errorResponse);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
    log.error("Handling unexpected exception", ex);

    ErrorResponse errorResponse = new ErrorResponse(
            ErrorCode.INTERNAL_SERVER_ERROR.getErrorCodeValue(),
            "An unexpected error occurred"
    );

    return ResponseEntity
            .status(ErrorCode.INTERNAL_SERVER_ERROR.getHttpStatus())
            .body(errorResponse);
  }
}