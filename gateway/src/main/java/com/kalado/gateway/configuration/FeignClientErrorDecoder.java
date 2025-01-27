package com.kalado.gateway.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kalado.common.response.ErrorResponse;
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

    try (InputStream bodyIs = response.body().asInputStream()) {
      errorResponse = objectMapper.readValue(bodyIs, ErrorResponse.class);
    } catch (IOException e) {
      log.warn("Could not decode error response body", e);
    }

    String message = errorResponse != null ? errorResponse.getMessage() : "";
    byte[] body = null;

    return switch (response.status()) {
      case 400 -> new feign.FeignException.BadRequest(
              methodKey,
              response.request(),
              body,
              Collections.emptyMap()
      );
      case 401 -> new feign.FeignException.Unauthorized(
              methodKey,
              response.request(),
              body,
              Collections.emptyMap()
      );
      case 403 -> new feign.FeignException.Forbidden(
              methodKey,
              response.request(),
              body,
              Collections.emptyMap()
      );
      case 404 -> new feign.FeignException.NotFound(
              methodKey,
              response.request(),
              body,
              Collections.emptyMap()
      );
      case 409 -> new feign.FeignException.Conflict(
              methodKey,
              response.request(),
              body,
              Collections.emptyMap()
      );
      default -> new feign.FeignException.FeignClientException(
              response.status(),
              methodKey,
              response.request(),
              body,
              Collections.emptyMap()
      );
    };
  }
}