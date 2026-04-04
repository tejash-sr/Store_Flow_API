package com.storeflow.storeflow_api.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when an invalid status transition is attempted.
 * Valid transitions: PENDING→CONFIRMED, CONFIRMED→SHIPPED, SHIPPED→DELIVERED,
 * PENDING→CANCELLED, CONFIRMED→CANCELLED
 * HTTP Status: 422 Unprocessable Entity
 */
public class InvalidStatusTransitionException extends AppException {
    public InvalidStatusTransitionException(String message) {
        super(message, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    public InvalidStatusTransitionException(String message, Throwable cause) {
        super(message, HttpStatus.UNPROCESSABLE_ENTITY, cause);
    }
}
