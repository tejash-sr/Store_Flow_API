package com.storeflow.storeflow_api.controller;

import com.storeflow.storeflow_api.dto.*;
import com.storeflow.storeflow_api.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication & Authorization REST Controller.
 * Handles user signup, login, token refresh, password reset, and profile management.
 * All endpoints except signup/login/refresh are protected by JWT authentication.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    /**
     * User signup endpoint.
     * Creates new user account with USER role and returns JWT tokens.
     * @param request SignupRequest with email, password, fullName
     * @return 201 CREATED with AuthResponse containing JWT tokens
     */
    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signup(@Valid @RequestBody SignupRequest request) {
        log.info("Signup request for email: {}", request.getEmail());
        AuthResponse response = authService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * User login endpoint.
     * Authenticates user by email/password and returns JWT tokens.
     * @param request LoginRequest with email, password
     * @return 200 OK with AuthResponse containing JWT tokens
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login request for email: {}", request.getEmail());
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Token refresh endpoint.
     * Returns new access token using valid refresh token.
     * @param request RefreshTokenRequest with refreshToken
     * @return 200 OK with AuthResponse containing new accessToken
     */
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        log.info("Token refresh request");
        AuthResponse response = authService.refresh(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Forgot password endpoint.
     * Generates password reset token and sends reset email.
     * @param request ForgotPasswordRequest with email
     * @return 202 ACCEPTED with success message
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        log.info("Forgot password request for email: {}", request.getEmail());
        String response = authService.forgotPassword(request);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    /**
     * Reset password endpoint.
     * Validates reset token and updates password.
     * @param request ResetPasswordRequest with token and newPassword
     * @return 200 OK with success message
     */
    @PostMapping("/reset-password/{token}")
    public ResponseEntity<String> resetPassword(@PathVariable String token, 
                                                @Valid @RequestBody ResetPasswordRequest request) {
        log.info("Reset password request with token: {}", token);
        request.setToken(token);
        String response = authService.resetPassword(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Get current user profile.
     * Requires valid JWT token in Authorization header.
     * @return 200 OK with UserResponse containing user details
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        log.info("Get profile request for user: {}", email);
        
        UserResponse response = authService.getCurrentUser(email);
        return ResponseEntity.ok(response);
    }

    /**
     * Upload user avatar.
     * Requires valid JWT token in Authorization header.
     * Placeholder for file upload - would integrate with FileStorageService.
     * @return 200 OK with success message
     */
    @PutMapping("/me/avatar")
    public ResponseEntity<String> uploadAvatar() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        log.info("Avatar upload request for user: {}", email);
        
        // TODO: Implement file upload with multipart/form-data
        return ResponseEntity.ok("Avatar upload endpoint - implementation pending");
    }
}
