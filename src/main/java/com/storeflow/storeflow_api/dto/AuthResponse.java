package com.storeflow.storeflow_api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * DTO for authentication response.
 * Returned after successful login/signup/refresh with JWT tokens.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {
    private String accessToken;   // JWT access token for API requests
    private String refreshToken;  // JWT refresh token for getting new access token
    private String tokenType;     // Always "Bearer"
    private Long expiresIn;       // Access token expiry in seconds
    private UserResponse user;    // User profile information

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserResponse {
        private String id;
        private String email;
        private String fullName;
        private String avatar;
        private Set<String> roles;
    }
}
