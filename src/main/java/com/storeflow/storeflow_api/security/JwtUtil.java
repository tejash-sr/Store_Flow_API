package com.storeflow.storeflow_api.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.*;

import jakarta.annotation.PostConstruct;

/**
 * Utility class for JWT token generation, validation, and claim extraction.
 * Handles both access tokens (short-lived) and refresh tokens (long-lived).
 */
@Component
@Slf4j
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.accessTokenExpiration:3600000}") // 1 hour default
    private Long accessTokenExpiration;

    @Value("${jwt.refreshTokenExpiration:604800000}") // 7 days default
    private Long refreshTokenExpiration;

    @PostConstruct
    public void validateConfig() {
        if (secretKey == null || secretKey.length() < 32) {
            throw new IllegalStateException("JWT secret must be set and at least 32 chars");
        }
    }

    /**
     * Generate JWT access token for authenticated user.
     * Short-lived token (typically 1 hour) for API request authentication.
     */
    public String generateAccessToken(String email, String userId, Set<String> roles) {
        return buildToken(email, userId, roles, accessTokenExpiration);
    }

    /**
     * Generate JWT refresh token for authenticated user.
     * Long-lived token (typically 7 days) for obtaining new access tokens.
     */
    public String generateRefreshToken(String email, String userId) {
        Map<String, Object> claims = new HashMap<>();
        return buildToken(email, userId, claims, refreshTokenExpiration);
    }

    /**
     * Validate JWT token signature and expiry.
     * Throws JwtException if token is invalid, expired, or malformed.
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("JWT token is expired: {}", e.getMessage());
            throw e;
        } catch (UnsupportedJwtException e) {
            log.error("JWT token is unsupported: {}", e.getMessage());
            throw e;
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
            throw e;
        } catch (SignatureException e) {
            log.error("JWT signature validation failed: {}", e.getMessage());
            throw e;
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Extract username/email from JWT token.
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extract user ID from JWT token (stored as 'userId' claim).
     */
    public String extractUserId(String token) {
        return extractClaim(token, claims -> claims.get("userId", String.class));
    }

    /**
     * Extract user roles from JWT token (stored as 'roles' claim).
     */
    @SuppressWarnings("unchecked")
    public Set<String> extractRoles(String token) {
        Object rolesObj = extractClaim(token, claims -> claims.get("roles"));
        if (rolesObj instanceof Collection<?>) {
            return new HashSet<>((Collection<String>) rolesObj);
        }
        return new HashSet<>();
    }

    /**
     * Extract expiry time from JWT token.
     */
    public Long extractExpiration(String token) {
        return extractClaim(token, claims -> claims.getExpiration().getTime());
    }

    /**
     * Generic method to extract any claim from JWT token.
     */
    private <T> T extractClaim(String token, java.util.function.Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Parse all claims from JWT token.
     */
    private Claims getAllClaims(String token) {
        return Jwts.parser()
            .verifyWith(getSigningKey())
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }

    /**
     * Build JWT token with given parameters and expiration.
     */
    private String buildToken(String email, String userId, Object rolesClaims, Long expiration) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        if (rolesClaims instanceof Set<?>) {
            claims.put("roles", rolesClaims);
        } else if (rolesClaims instanceof Map<?, ?>) {
            @SuppressWarnings("unchecked")
            Map<String, Object> claimsMap = (Map<String, Object>) rolesClaims;
            claims.putAll(claimsMap);
        }

        return Jwts.builder()
            .claims(claims)
            .subject(email)
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + expiration))
            .signWith(getSigningKey(), SignatureAlgorithm.HS256)
            .compact();
    }

    /**
     * Get HMAC signing key from secret string.
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}