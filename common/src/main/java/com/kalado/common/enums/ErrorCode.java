package com.kalado.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR, 500, "Internal_server_error"),
    SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, HttpStatus.SERVICE_UNAVAILABLE, 503, "Service_unavailable"),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, HttpStatus.UNAUTHORIZED, 401, "Unauthorized"),
    FORBIDDEN(HttpStatus.FORBIDDEN, HttpStatus.FORBIDDEN, 403, "Forbidden"),
    INVALID_TOKEN(HttpStatus.FORBIDDEN, HttpStatus.FORBIDDEN, 403, "Invalid_token"),
    BAD_REQUEST(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST, 400, "Bad_request"),
    USER_ALREADY_EXISTS(HttpStatus.CONFLICT, HttpStatus.CONFLICT, 409, "User_already_exists"),
    NO_CONTENT(HttpStatus.NO_CONTENT, HttpStatus.NO_CONTENT, 204, "No_content"),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, HttpStatus.UNAUTHORIZED, 401, "Invalid_credentials"),
    NOT_FOUND(HttpStatus.NOT_FOUND, HttpStatus.NOT_FOUND, 404, "Resource_not_found"),
    NOT_FOUND_NO_CONTENT(HttpStatus.NOT_FOUND, HttpStatus.NO_CONTENT, 204, "Resource_not_found"),
    CONFLICT(HttpStatus.CONFLICT, HttpStatus.CONFLICT, 409, "Conflict"),
    UNPROCESSABLE_ENTITY(HttpStatus.UNPROCESSABLE_ENTITY, HttpStatus.UNPROCESSABLE_ENTITY, 422, "Unprocessable_entity"),
    PAYMENT_REQUIRED(HttpStatus.PAYMENT_REQUIRED, HttpStatus.PAYMENT_REQUIRED, 402, "Payment_required"),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, HttpStatus.METHOD_NOT_ALLOWED, 405, "Method_not_allowed"),
    UNSUPPORTED_MEDIA_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE, HttpStatus.UNSUPPORTED_MEDIA_TYPE, 415, "Unsupported_media_type"),
    TOO_MANY_REQUESTS(HttpStatus.TOO_MANY_REQUESTS, HttpStatus.TOO_MANY_REQUESTS, 429, "Too_many_requests"),
    EMAIL_NOT_VERIFIED(HttpStatus.UNAUTHORIZED, HttpStatus.UNAUTHORIZED, 401, "Email_not_verified");

    private final HttpStatus httpStatus;
    private final HttpStatus clientHttpStatus;
    private final Integer errorCodeValue;
    private final String errorMessageValue;
    private final String message;

    ErrorCode(HttpStatus httpStatus, HttpStatus clientHttpStatus, Integer errorCodeValue, String errorMessageValue) {
        this.httpStatus = httpStatus;
        this.clientHttpStatus = clientHttpStatus;
        this.errorCodeValue = errorCodeValue;
        this.errorMessageValue = errorMessageValue;
        this.message = null;
    }

    ErrorCode(String message) {
        this.httpStatus = null;
        this.clientHttpStatus = null;
        this.errorCodeValue = null;
        this.errorMessageValue = null;
        this.message = message;
    }
}
