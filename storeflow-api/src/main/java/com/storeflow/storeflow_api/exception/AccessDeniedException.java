package com.storeflow.storeflow_api.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when an authenticated user lacks permission for an operation.
 * Returns HTTP 403 Forbidden.
 */
public class AccessDeniedException extends AppException {
    public AccessDeniedException(String message) {
        super(message, HttpStatus.FORBIDDEN);
    }

    public AccessDeniedException(String message, Throwable cause) {
        super(message, HttpStatus.FORBIDDEN, cause);
    }
}
