package com.storeflow.storeflow_api.exception;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.WebRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for GlobalExceptionHandler in isolation (no Spring context).
 * Per PDF Phase 1 requirement P1-2: tests that the handler returns HTTP 500 for
 * generic RuntimeException and preserves custom status codes for domain exceptions,
 * and that the error body always has the consistent shape:
 * { timestamp, status, error, message, path }.
 */
@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerUnitTest {

    @InjectMocks
    private GlobalExceptionHandler handler;

    @Test
    void handleGenericException_returns500WithConsistentShape() {
        WebRequest req = mock(WebRequest.class);
        when(req.getDescription(false)).thenReturn("uri=/api/test");

        ResponseEntity<ErrorResponse> response =
                handler.handleGenericException(new RuntimeException("boom"), req);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getTimestamp()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(500);
        assertThat(response.getBody().getMessage()).isNotNull();
        assertThat(response.getBody().getPath()).isEqualTo("/api/test");
        assertThat(response.getBody().getError()).isEqualTo("Internal Server Error");
    }

    @Test
    void handleGenericException_sanitisesUriPrefix() {
        WebRequest req = mock(WebRequest.class);
        when(req.getDescription(false)).thenReturn("uri=/api/orders/99");

        ResponseEntity<ErrorResponse> response =
                handler.handleGenericException(new RuntimeException("oops"), req);

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getPath()).isEqualTo("/api/orders/99");
    }

    @Test
    void handleAppException_preservesCustomStatusCode() {
        WebRequest req = mock(WebRequest.class);
        when(req.getDescription(false)).thenReturn("uri=/api/products/1");

        ResponseEntity<ErrorResponse> response =
                handler.handleAppException(new ResourceNotFoundException("Product not found"), req);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(404);
        assertThat(response.getBody().getMessage()).isEqualTo("Product not found");
        assertThat(response.getBody().getPath()).isEqualTo("/api/products/1");
        assertThat(response.getBody().getTimestamp()).isNotNull();
    }

    @Test
    void handleIllegalArgumentException_returns409Conflict() {
        WebRequest req = mock(WebRequest.class);
        when(req.getDescription(false)).thenReturn("uri=/api/auth/signup");

        ResponseEntity<ErrorResponse> response =
                handler.handleIllegalArgument(new IllegalArgumentException("Email already exists"), req);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(409);
        assertThat(response.getBody().getMessage()).isEqualTo("Email already exists");
    }

    @Test
    void handleAppException_withNullMessage_doesNotThrow() {
        WebRequest req = mock(WebRequest.class);
        when(req.getDescription(false)).thenReturn("uri=/api/x");

        // AccessDeniedException from the domain (403), not spring-security's AccessDeniedException
        ResponseEntity<ErrorResponse> response =
                handler.handleAppException(new AccessDeniedException("Access denied"), req);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getTimestamp()).isNotNull();
    }
}
