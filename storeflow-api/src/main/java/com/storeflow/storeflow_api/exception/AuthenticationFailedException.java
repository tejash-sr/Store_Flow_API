package com.storeflow.storeflow_api.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when authentication fails.
 * Covers invalid credentials, expired tokens, etc.
 * HTTP Status: 401 Unauthorized
 */
public class AuthenticationFailedException extends AppException {
    public AuthenticationFailedException(String message) {
        super(message, HttpStatus.UNAUTHORIZED);
    }

    public AuthenticationFailedException(String message, Throwable cause) {
        super(message, HttpStatus.UNAUTHORIZED, cause);
    }
}
