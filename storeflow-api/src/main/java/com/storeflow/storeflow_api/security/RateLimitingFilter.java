package com.storeflow.storeflow_api.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate Limiting Filter using Bucket4j token bucket algorithm.
 * Limits authentication requests (signup, login, forgot-password) to 5 requests per 15 minutes per IP.
 * Returns 429 Too Many Requests if limit exceeded.
 */
@Component
@Slf4j
public class RateLimitingFilter extends OncePerRequestFilter {

    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

    @Autowired
    private Environment environment;

    /**
     * Token bucket configuration: 5 tokens per 15 minutes per IP address.
     */
    private Bucket createNewBucket() {
        Bandwidth limit = Bandwidth.classic(5, Refill.intervally(5, Duration.ofMinutes(15)));
        return Bucket4j.builder()
            .addLimit(limit)
            .build();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        
        // Skip rate limiting in test profile
        if (environment != null && environment.matchesProfiles("test")) {
            filterChain.doFilter(request, response);
            return;
        }
        
        String requestPath = request.getRequestURI();
        
        // Only apply rate limiting to auth endpoints
        if (isAuthEndpoint(requestPath)) {
            String clientIp = getClientIp(request);
            Bucket bucket = resolveBucket(clientIp);

            if (!bucket.tryConsume(1)) {
                // Rate limit exceeded
                log.warn("Rate limit exceeded for IP: {} on path: {}", clientIp, requestPath);
                response.setStatus(429); // Too Many Requests
                response.setContentType("application/json");
                response.getWriter().write("{\"error\": \"Too many requests. Please try again later.\"}");
                return;
            }

            log.debug("Rate limit check passed for IP: {}", clientIp);
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Check if request is to a rate-limited auth endpoint.
     */
    private boolean isAuthEndpoint(String path) {
        return path.startsWith("/api/auth/signup") ||
               path.startsWith("/api/auth/login") ||
               path.startsWith("/api/auth/forgot-password") ||
               path.startsWith("/api/auth/reset-password");
    }

    /**
     * Get or create token bucket for client IP address.
     */
    private Bucket resolveBucket(String key) {
        return cache.computeIfAbsent(key, k -> createNewBucket());
    }

    /**
     * Extract client IP from request headers or remote address.
     * Checks X-Forwarded-For header first (for proxy scenarios), then remote address.
     */
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            // X-Forwarded-For can contain multiple IPs, take the first one
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
