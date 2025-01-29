package com.kalado.gateway.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kalado.common.exception.GlobalExceptionHandler;
import com.kalado.common.response.ErrorResponse;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import java.io.IOException;
import java.time.LocalDateTime;

@RestControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class GatewayExceptionHandler extends GlobalExceptionHandler {
    private final ObjectMapper objectMapper;

    @Override
    @ExceptionHandler(FeignException.class)
    public ResponseEntity<ErrorResponse> handleFeignException(FeignException ex, WebRequest request) {
        ErrorResponse errorResponse;
        try {
            String responseBody = ex.contentUTF8();
            errorResponse = objectMapper.readValue(responseBody, ErrorResponse.class);

            errorResponse.setPath(((ServletWebRequest) request).getRequest().getRequestURI());
        } catch (IOException e) {
            log.warn("Could not parse error response, using default", e);
            errorResponse = ErrorResponse.builder()
                    .errorCode(ex.status())
                    .message(ex.getMessage())
                    .timestamp(LocalDateTime.now().toString())
                    .status(ex.status())
                    .error(HttpStatus.valueOf(ex.status()).getReasonPhrase())
                    .path(((ServletWebRequest) request).getRequest().getRequestURI())
                    .build();
        }

        return ResponseEntity
                .status(ex.status())
                .body(errorResponse);
    }

    @ExceptionHandler(CustomGatewayException.class)
    public ResponseEntity<ErrorResponse> handleCustomGatewayException(
            CustomGatewayException ex,
            WebRequest request
    ) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode(ex.getErrorCode().getErrorCodeValue())
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now().toString())
                .status(ex.getErrorCode().getHttpStatus().value())
                .error(ex.getErrorCode().name())
                .path(((ServletWebRequest) request).getRequest().getRequestURI())
                .build();

        return ResponseEntity
                .status(ex.getErrorCode().getHttpStatus())
                .body(errorResponse);
    }
}