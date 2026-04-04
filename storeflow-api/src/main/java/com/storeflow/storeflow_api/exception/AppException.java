package com.storeflow.storeflow_api.exception;

import org.springframework.http.HttpStatus;

/**
 * Base exception for application-specific errors.
 * All custom exceptions should extend this class.
 */
public class AppException extends RuntimeException {
    private final HttpStatus httpStatus;

    public AppException(String message, HttpStatus httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
    }

    public AppException(String message, HttpStatus httpStatus, Throwable cause) {
        super(message, cause);
        this.httpStatus = httpStatus;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
