package com.storeflow.storeflow_api.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when stock is insufficient for an operation.
 * Returns HTTP 409 Conflict.
 */
public class InsufficientStockException extends AppException {
    public InsufficientStockException(String message) {
        super(message, HttpStatus.CONFLICT);
    }

    public InsufficientStockException(String message, Throwable cause) {
        super(message, HttpStatus.CONFLICT, cause);
    }
}
