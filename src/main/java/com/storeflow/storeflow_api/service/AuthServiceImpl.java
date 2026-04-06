package com.storeflow.storeflow_api.service;

import com.storeflow.storeflow_api.dto.*;
import com.storeflow.storeflow_api.entity.User;
import com.storeflow.storeflow_api.entity.UserRole;
import com.storeflow.storeflow_api.entity.UserStatus;
import com.storeflow.storeflow_api.exception.AuthenticationFailedException;
import com.storeflow.storeflow_api.repository.UserRepository;
import com.storeflow.storeflow_api.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of AuthService for JWT-based authentication.
 * Handles user registration, login, token refresh, and password reset flows.
 */
@Service
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public AuthServiceImpl(
            UserRepository userRepository,
            JwtUtil jwtUtil,
            PasswordEncoder passwordEncoder,
            @org.springframework.beans.factory.annotation.Qualifier("primaryEmailService") EmailService emailService) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    /**
     * Register new user with email, password, and full name.
     * Email must be unique (case-insensitive).
     * Password is hashed before persistence.
     * Returns JWT tokens for immediate use.
     */
    @Override
    @Transactional
    public AuthResponse signup(SignupRequest request) {
        // Check if email already exists
        if (userRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }

        // Hash password using BCrypt
        String hashedPassword = passwordEncoder.encode(request.getPassword());

        // Create new user with USER role by default
        User user = User.builder()
            .email(request.getEmail().toLowerCase())
            .password(hashedPassword)
            .fullName(request.getFullName())
            .roles(new HashSet<>(List.of(UserRole.ROLE_USER)))
            .status(UserStatus.ACTIVE)
            .build();

        User savedUser = userRepository.save(user);
        log.info("User registered successfully: {}", savedUser.getEmail());

        // Send welcome email (real implementation, mocked in tests)
        emailService.sendWelcomeEmail(savedUser.getEmail(), savedUser.getFullName());

        // Generate tokens
        String accessToken = jwtUtil.generateAccessToken(
            savedUser.getEmail(), 
            savedUser.getId().toString(),
            rolesToStrings(savedUser.getRoles())
        );
        String refreshToken = jwtUtil.generateRefreshToken(
            savedUser.getEmail(), 
            savedUser.getId().toString()
        );

        return buildAuthResponse(savedUser, accessToken, refreshToken);
    }

    /**
     * Authenticate user by email and password.
     * Checks if user exists, is active, and password matches.
     * Returns JWT tokens on success.
     * Throws AuthenticationFailedException (401) if credentials are invalid.
     */
    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        // Find active user by email
        User user = userRepository.findByEmailIgnoreCaseAndStatus(
            request.getEmail(), 
            UserStatus.ACTIVE
        ).orElseThrow(() -> {
            log.warn("Login failed: user not found or inactive - {}", request.getEmail());
            return new AuthenticationFailedException("Invalid email or password");
        });

        // Validate password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("Login failed: invalid password - {}", request.getEmail());
            throw new AuthenticationFailedException("Invalid email or password");
        }

        log.info("User logged in successfully: {}", user.getEmail());

        // Generate tokens
        String accessToken = jwtUtil.generateAccessToken(
            user.getEmail(), 
            user.getId().toString(),
            rolesToStrings(user.getRoles())
        );
        String refreshToken = jwtUtil.generateRefreshToken(
            user.getEmail(), 
            user.getId().toString()
        );

        return buildAuthResponse(user, accessToken, refreshToken);
    }

    /**
     * Generate new access token using valid refresh token.
     * Validates that refresh token belongs to an active user.
     */
    @Override
    @Transactional(readOnly = true)
    public AuthResponse refresh(RefreshTokenRequest request) {
        try {
            // Validate refresh token
            jwtUtil.validateToken(request.getRefreshToken());
            
            String email = jwtUtil.extractUsername(request.getRefreshToken());
            
            User user = userRepository.findByEmailIgnoreCaseAndStatus(
                email, 
                UserStatus.ACTIVE
            ).orElseThrow(() -> new RuntimeException("User not found or inactive"));

            // Generate new access token
            String accessToken = jwtUtil.generateAccessToken(
                user.getEmail(), 
                user.getId().toString(),
                rolesToStrings(user.getRoles())
            );

            log.info("Token refreshed for user: {}", email);

            return buildAuthResponse(user, accessToken, request.getRefreshToken());
        } catch (Exception e) {
            log.error("Token refresh failed: {}", e.getMessage());
            throw new RuntimeException("Invalid refresh token");
        }
    }

    /**
     * Initiate password reset by sending reset token via email.
     * Generates time-limited reset token (valid for 24 hours).
     * TODO: In tests, email sending is mocked.
     */
    @Override
    @Transactional
    public String forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmailIgnoreCase(request.getEmail())
            .orElseThrow(() -> {
                log.warn("Forgot password: user not found - {}", request.getEmail());
                return new RuntimeException("User not found");
            });

        // Generate reset token (simple UUID-based for demo)
        String resetToken = UUID.randomUUID().toString();
        LocalDateTime expiryTime = LocalDateTime.now().plusHours(24);

        user.setPasswordResetToken(resetToken);
        user.setPasswordResetTokenExpiry(expiryTime);
        userRepository.save(user);

        // Send email with reset link (real implementation, mocked in tests)
        emailService.sendPasswordResetEmail(user.getEmail(), user.getFullName(), resetToken);

        log.info("Password reset email sent to: {}", request.getEmail());
        return "Password reset email sent";
    }

    /**
     * Complete password reset using valid reset token.
     * Updates password and invalidates reset token.
     */
    @Override
    @Transactional
    public String resetPassword(ResetPasswordRequest request) {
        User user = userRepository.findByPasswordResetToken(request.getToken())
            .orElseThrow(() -> new RuntimeException("Invalid reset token"));

        // Check if token is expired
        if (!user.isResetTokenValid()) {
            throw new RuntimeException("Reset token has expired");
        }

        // Update password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setPasswordResetToken(null);
        user.setPasswordResetTokenExpiry(null);
        userRepository.save(user);

        // Send password changed confirmation email (real implementation, mocked in tests)
        emailService.sendPasswordChangedEmail(user.getEmail(), user.getFullName());

        log.info("Password reset successfully for user: {}", user.getEmail());
        return "Password reset successfully";
    }

    /**
     * Get current user profile by email.
     */
    @Override
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(String email) {
        User user = userRepository.findByEmailIgnoreCase(email)
            .orElseThrow(() -> new RuntimeException("User not found"));

        return mapUserToResponse(user);
    }

    /**
     * Validate user credentials without throwing exception on failure.
     */
    @Override
    @Transactional(readOnly = true)
    public boolean validateCredentials(String email, String password) {
        return userRepository.findByEmailIgnoreCase(email)
            .map(user -> passwordEncoder.matches(password, user.getPassword()))
            .orElse(false);
    }

    // ===== Helper Methods =====

    /**
     * Convert User roles to string set for JWT claim.
     */
    private Set<String> rolesToStrings(Set<UserRole> roles) {
        return roles.stream()
            .map(UserRole::name)
            .collect(Collectors.toSet());
    }

    /**
     * Build AuthResponse from User and tokens.
     */
    private AuthResponse buildAuthResponse(User user, String accessToken, String refreshToken) {
        return AuthResponse.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .tokenType("Bearer")
            .expiresIn(3600L) // 1 hour in seconds
            .user(AuthResponse.UserResponse.builder()
                .id(user.getId().toString())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .avatar(user.getAvatar())
                .roles(rolesToStrings(user.getRoles()))
                .build())
            .build();
    }

    /**
     * Map User entity to UserResponse DTO.
     */
    private UserResponse mapUserToResponse(User user) {
        return UserResponse.builder()
            .id(user.getId().toString())
            .email(user.getEmail())
            .fullName(user.getFullName())
            .avatar(user.getAvatar())
            .roles(rolesToStrings(user.getRoles()))
            .status(user.getStatus().name())
            .createdAt(user.getCreatedAt().toString())
            .build();
    }
}
