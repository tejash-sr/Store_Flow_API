package com.storeflow.storeflow_api.config;

import com.storeflow.storeflow_api.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.server.WebGraphQlInterceptor;
import org.springframework.graphql.server.WebGraphQlRequest;
import org.springframework.graphql.server.WebGraphQlResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * GraphQL Configuration
 * - Enables authentication context via interceptor
 * - Validates JWT tokens at WebSocket handshake
 */
@Slf4j
@Configuration
public class GraphQlConfig {
    
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    
    public GraphQlConfig(JwtUtil jwtUtil, UserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }
    
    /**
     * WebGraphQL Interceptor for JWT authentication
     * Extracts Authorization header and sets SecurityContext
     */
    @Bean
    public WebGraphQlInterceptor jwtGraphQlInterceptor() {
        return new WebGraphQlInterceptor() {
            @Override
            public Mono<WebGraphQlResponse> intercept(WebGraphQlRequest request, Chain chain) {
                // Extract Authorization header
                String authHeader = request.getHeaders().getFirst("Authorization");
                
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    String token = authHeader.substring(7); // Remove "Bearer " prefix
                    try {
                        String username = jwtUtil.extractUsername(token);
                        if (username != null) {
                            var userDetails = userDetailsService.loadUserByUsername(username);
                            Authentication auth = new UsernamePasswordAuthenticationToken(
                                    userDetails, null, userDetails.getAuthorities()
                            );
                            SecurityContextHolder.getContext().setAuthentication(auth);
                            log.debug("GraphQL Authentication set for user: {}", username);
                        }
                    } catch (Exception e) {
                        log.warn("GraphQL JWT validation failed: {}", e.getMessage());
                    }
                }
                
                return chain.next(request)
                        .doFinally(signalType -> SecurityContextHolder.clearContext());
            }
        };
    }
}
