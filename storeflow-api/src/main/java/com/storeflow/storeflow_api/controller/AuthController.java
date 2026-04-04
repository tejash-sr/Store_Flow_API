package com.storeflow.storeflow_api.controller;

import com.storeflow.storeflow_api.dto.*;
import com.storeflow.storeflow_api.service.AuthService;
import com.storeflow.storeflow_api.service.FileStorageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

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
    private final FileStorageService fileStorageService;

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
     * Resizes image to 200x200 using Thumbnailator.
     * @param file MultipartFile containing avatar image
     * @return 200 OK with success message and file path
     */
    @PutMapping("/me/avatar")
    public ResponseEntity<?> uploadAvatar(@RequestParam(name = "file", required = false) MultipartFile file) {
        try {
            if (file == null || file.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("{ \"error\": \"No file provided\" }");
            }

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();
            log.info("Avatar upload request for user: {}", email);

            // Validate file
            if (file.getSize() > 5 * 1024 * 1024) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("{ \"error\": \"File size exceeds 5MB limit\" }");
            }

            String contentType = file.getContentType();
            if (contentType == null || (!contentType.contains("image/jpeg") && !contentType.contains("image/png") && !contentType.contains("image/webp"))) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("{ \"error\": \"Invalid file type. Allowed: JPEG, PNG, WebP\" }");
            }

            // Resize avatar to 200x200 using Thumbnailator
            byte[] resizedImageBytes = resizeAvatar(file.getBytes());

            // Create a new MultipartFile wrapper for the resized image
            String fileName = file.getOriginalFilename();
            MultipartFile resizedFile = new ResizedMultipartFile(resizedImageBytes, fileName, contentType);

            // Save resized avatar
            String filePath = fileStorageService.saveUserAvatar(resizedFile, email);
            log.info("Avatar uploaded and resized for user: {}", email);

            return ResponseEntity.ok()
                .body("{ \"message\": \"Avatar uploaded successfully\", \"filePath\": \"" + filePath + "\" }");

        } catch (IllegalArgumentException e) {
            log.warn("Invalid avatar file: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("{ \"error\": \"" + e.getMessage() + "\" }");
        } catch (IOException e) {
            log.error("Failed to upload avatar", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("{ \"error\": \"Failed to upload avatar\" }");
        }
    }

    /**
     * Resize avatar image to 200x200 pixels using Thumbnailator
     */
    private byte[] resizeAvatar(byte[] imageBytes) throws IOException {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(imageBytes);
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            
            Thumbnails.of(inputStream)
                .size(200, 200)
                .keepAspectRatio(true)
                .toOutputStream(outputStream);
            
            return outputStream.toByteArray();
        }
    }

    /**
     * Simple MultipartFile wrapper for resized image bytes
     */
    private static class ResizedMultipartFile implements MultipartFile {
        private final byte[] bytes;
        private final String name;
        private final String contentType;

        ResizedMultipartFile(byte[] bytes, String name, String contentType) {
            this.bytes = bytes;
            this.name = name;
            this.contentType = contentType;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getOriginalFilename() {
            return name;
        }

        @Override
        public String getContentType() {
            return contentType;
        }

        @Override
        public boolean isEmpty() {
            return bytes.length == 0;
        }

        @Override
        public long getSize() {
            return bytes.length;
        }

        @Override
        public byte[] getBytes() throws IOException {
            return bytes;
        }

        @Override
        public java.io.InputStream getInputStream() throws IOException {
            return new ByteArrayInputStream(bytes);
        }

        @Override
        public void transferTo(java.io.File dest) throws IOException, IllegalStateException {
            java.nio.file.Files.write(dest.toPath(), bytes);
        }
    }
}
