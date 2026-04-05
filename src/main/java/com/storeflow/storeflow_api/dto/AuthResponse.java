package com.storeflow.storeflow_api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Authentication response containing JWT tokens and user information")
public class AuthResponse {
    @Schema(description = "JWT access token for API requests", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String accessToken;   // JWT access token for API requests
    
    @Schema(description = "JWT refresh token for getting new access token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String refreshToken;  // JWT refresh token for getting new access token
    
    @Schema(description = "Token type (always Bearer)", example = "Bearer")
    private String tokenType;     // Always "Bearer"
    
    @Schema(description = "Access token expiry in seconds", example = "3600")
    private Long expiresIn;       // Access token expiry in seconds
    
    @Schema(description = "User profile information")
    private UserResponse user;    // User profile information

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Authenticated user information")
    public static class UserResponse {
        @Schema(description = "Unique user identifier", example = "1")
        private String id;
        private String email;
        private String fullName;
        private String avatar;
        private Set<String> roles;
    }
}
