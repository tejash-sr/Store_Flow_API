package com.storeflow.storeflow_api.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when a user attempts to access a resource
 * they do not have permission to access.
 * HTTP Status: 403 Forbidden
 */
public class AccessDeniedException extends AppException {
    public AccessDeniedException(String message) {
        super(message, HttpStatus.FORBIDDEN);
    }

    public AccessDeniedException(String message, Throwable cause) {
        super(message, HttpStatus.FORBIDDEN, cause);
    }
}
