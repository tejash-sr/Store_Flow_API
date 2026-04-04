package com.storeflow.storeflow_api.service;

import com.storeflow.storeflow_api.dto.*;

/**
 * Service interface for authentication and authorization operations.
 * Handles user registration, login, token refresh, and password management.
 */
public interface AuthService {

    /**
     * Register a new user with email, password, and full name.
     * Password is hashed using BCrypt before storage.
     * Returns JWT access and refresh tokens for immediate usage.
     */
    AuthResponse signup(SignupRequest request);

    /**
     * Authenticate user by email and password.
     * Validates credentials and returns JWT tokens if successful.
     * Throws AuthenticationFailedException if credentials are invalid.
     */
    AuthResponse login(LoginRequest request);

    /**
     * Generate new access token using valid refresh token.
     * Refresh token must not be expired.
     * Returns new access token with updated expiry.
     */
    AuthResponse refresh(RefreshTokenRequest request);

    /**
     * Initiate password reset flow for user with given email.
     * Generates reset token and sends it via email (mocked in tests).
     * 
     * @param request Contains email address
     * @return Message confirming email was sent
     */
    String forgotPassword(ForgotPasswordRequest request);

    /**
     * Complete password reset using valid reset token.
     * Validates token expiry and resets password to new value.
     * 
     * @param request Contains reset token and new password
     * @return Confirmation message
     */
    String resetPassword(ResetPasswordRequest request);

    /**
     * Get current user profile by email.
     * Used in GET /api/auth/me endpoint.
     */
    UserResponse getCurrentUser(String email);

    /**
     * Validate user credentials (email and password).
     * Returns true if credentials are correct, false otherwise.
     * Does not throw exception for invalid credentials.
     */
    boolean validateCredentials(String email, String password);
}
