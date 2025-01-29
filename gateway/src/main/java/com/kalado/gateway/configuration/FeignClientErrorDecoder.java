package com.kalado.gateway.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kalado.common.response.ErrorResponse;
import feign.FeignException;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;

@Slf4j
@RequiredArgsConstructor
public class FeignClientErrorDecoder implements ErrorDecoder {
  private final ObjectMapper objectMapper;

  @Override
  public Exception decode(String methodKey, Response response) {
    ErrorResponse errorResponse = null;
    byte[] bodyData = null;

    try (InputStream bodyIs = response.body().asInputStream()) {
      bodyData = bodyIs.readAllBytes();
      errorResponse = objectMapper.readValue(bodyData, ErrorResponse.class);
    } catch (IOException e) {
      log.warn("Could not decode error response body", e);
    }

    String message = errorResponse != null ? errorResponse.getMessage() : "";

    return switch (response.status()) {
      case 400 -> new FeignException.BadRequest(
              message,
              response.request(),
              bodyData,
              response.headers()
      );
      case 401 -> new FeignException.Unauthorized(
              message,
              response.request(),
              bodyData,
              response.headers()
      );
      case 403 -> new FeignException.Forbidden(
              message,
              response.request(),
              bodyData,
              response.headers()
      );
      case 404 -> new FeignException.NotFound(
              message,
              response.request(),
              bodyData,
              response.headers()
      );
      default -> new FeignException.FeignServerException(
              response.status(),
              message,
              response.request(),
              bodyData,
              response.headers()
      );
    };
  }
}