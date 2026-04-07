package com.storeflow.storeflow_api.security;

import com.storeflow.storeflow_api.entity.User;
import com.storeflow.storeflow_api.repository.UserRepository;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;
import java.util.Optional;

/**
 * WebSocket handshake interceptor for JWT authentication.
 * 
 * Validates JWT tokens before allowing WebSocket connections. Extracts the token from:
 * 1. Authorization header (Bearer token)
 * 2. Query parameter (?token=...)
 * 
 * On successful validation, stores user attributes (userId, email, role) in the WebSocket
 * session attributes for downstream use in message handlers.
 * 
 * On validation failure, rejects the handshake with 401 Unauthorized.
 * 
 * @author StoreFlow
 * @version 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements HandshakeInterceptor {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    /**
     * Intercept the handshake before the connection is established.
     * 
     * Validates the JWT token and loads the authenticated user. Rejects the connection
     * if the token is missing, invalid, or expired.
     * 
     * @param request the HTTP upgrade request
     * @param response the HTTP upgrade response (used to send 401 if needed)
     * @param wsHandler the WebSocket handler
     * @param attributes session attributes map (for storing validated user info)
     * @return true if handshake should proceed, false to reject
     */
    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        try {
            // Extract JWT token from Authorization header or query parameter
            String token = extractToken(request);
            
            if (token == null) {
                log.warn("WebSocket connection rejected: No JWT token provided");
                response.setStatusCode(org.springframework.http.HttpStatus.UNAUTHORIZED);
                return false;
            }

            // Validate JWT token signature and expiry
            jwtUtil.validateToken(token);
            
            // Extract username from token
            String username = jwtUtil.extractUsername(token);

            // Load user from database
            Optional<User> userOptional = userRepository.findByEmailIgnoreCase(username);
            
            if (userOptional.isEmpty()) {
                log.warn("WebSocket connection rejected: User not found for username {}", username);
                response.setStatusCode(org.springframework.http.HttpStatus.UNAUTHORIZED);
                return false;
            }

            // Store user info in session attributes for downstream access
            User user = userOptional.get();
            attributes.put("userId", user.getId());
            attributes.put("email", user.getEmail());
            attributes.put("roles", user.getRoles());
            
            log.info("WebSocket connection authenticated for user: {}", username);
            return true;

        } catch (JwtException ex) {
            log.warn("WebSocket authentication error - Invalid JWT: {}", ex.getMessage());
            response.setStatusCode(org.springframework.http.HttpStatus.UNAUTHORIZED);
            return false;
        } catch (Exception ex) {
            log.error("Unexpected error during WebSocket handshake", ex);
            response.setStatusCode(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR);
            return false;
        }
    }

    /**
     * Callback after successful handshake completion (no cleanup required).
     * 
     * @param request the HTTP upgrade request
     * @param response the HTTP upgrade response
     * @param wsHandler the WebSocket handler
     * @param exception any exception thrown during connection
     */
    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        // No post-handshake processing needed
    }

    /**
     * Extract JWT token from the WebSocket upgrade request.
     * 
     * Attempts to extract from:
     * 1. Authorization header with Bearer scheme
     * 2. Query parameter 'token'
     * 
     * @param request the HTTP upgrade request
     * @return the JWT token, or null if not found
     */
    private String extractToken(ServerHttpRequest request) {
        // Try Authorization header first (Bearer token)
        String authHeader = request.getHeaders().getFirst("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7); // Remove "Bearer " prefix
        }

        // Fallback to query parameter
        String uri = request.getURI().toString();
        if (uri.contains("token=")) {
            int start = uri.indexOf("token=") + 6;
            int end = uri.indexOf("&", start);
            if (end == -1) {
                end = uri.length();
            }
            return uri.substring(start, end);
        }

        return null;
    }
}
