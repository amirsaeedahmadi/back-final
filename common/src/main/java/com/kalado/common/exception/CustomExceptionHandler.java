package com.kalado.common.exception;

import com.kalado.common.enums.ErrorCode;
import com.kalado.common.response.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;

@Slf4j
@ControllerAdvice
public class CustomExceptionHandler {

  @ExceptionHandler(CustomException.class)
  public ResponseEntity<ErrorResponse> handleCustomException(CustomException ex) {
    ErrorResponse errorResponse = new ErrorResponse(
            ex.getErrorCode().getErrorCodeValue(),
            ex.getMessage()
    );
    return new ResponseEntity<>(errorResponse, ex.getErrorCode().getHttpStatus());
  }

  @ExceptionHandler(MaxUploadSizeExceededException.class)
  public ResponseEntity<ErrorResponse> handleMaxSizeException(MaxUploadSizeExceededException e) {
    ErrorResponse errorResponse = new ErrorResponse(
            ErrorCode.BAD_REQUEST.getErrorCodeValue(),
            "File size exceeds maximum limit"
    );
    return new ResponseEntity<>(errorResponse, ErrorCode.BAD_REQUEST.getHttpStatus());
  }

  @ExceptionHandler(MultipartException.class)
  public ResponseEntity<ErrorResponse> handleMultipartException(MultipartException e) {
    ErrorResponse errorResponse = new ErrorResponse(
            ErrorCode.BAD_REQUEST.getErrorCodeValue(),
            "Error processing file upload"
    );
    return new ResponseEntity<>(errorResponse, ErrorCode.BAD_REQUEST.getHttpStatus());
  }
}