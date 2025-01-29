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

    ROLE_CHANGE_NOT_ALLOWED(HttpStatus.FORBIDDEN, HttpStatus.FORBIDDEN, 403, "Role_change_not_allowed"),
    INVALID_ROLE_TRANSITION(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST, 400, "Invalid_role_transition"),
    UNAUTHORIZED_ROLE_ACCESS(HttpStatus.FORBIDDEN, HttpStatus.FORBIDDEN, 403, "Unauthorized_role_access"),
    GOD_ROLE_MODIFICATION_FORBIDDEN(HttpStatus.FORBIDDEN, HttpStatus.FORBIDDEN, 403, "God_role_modification_forbidden"),
    EMAIL_NOT_VERIFIED(HttpStatus.UNAUTHORIZED, HttpStatus.UNAUTHORIZED, 401, "Email_not_verified"),
    INSUFFICIENT_PRIVILEGES(HttpStatus.FORBIDDEN, HttpStatus.FORBIDDEN, 403, "Insufficient_privileges");

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