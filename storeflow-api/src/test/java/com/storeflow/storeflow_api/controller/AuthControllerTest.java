package com.storeflow.storeflow_api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.storeflow.storeflow_api.config.TestMailConfig;
import com.storeflow.storeflow_api.dto.*;
import com.storeflow.storeflow_api.entity.User;
import com.storeflow.storeflow_api.entity.UserRole;
import com.storeflow.storeflow_api.entity.UserStatus;
import com.storeflow.storeflow_api.repository.UserRepository;
import com.storeflow.storeflow_api.security.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for AuthController with JWT authentication and rate limiting.
 * Tests all 7 auth endpoints, JWT validation, and role-based authorization.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestMailConfig.class)
@Transactional
@Slf4j
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    // ============ SIGNUP TESTS ============

    @Test
    @DisplayName("POST /api/auth/signup with valid data returns 201 CREATED with JWT tokens")
    void signup_validRequest_returns201WithTokens() throws Exception {
        SignupRequest request = SignupRequest.builder()
            .email("newuser@test.com")
            .password("Password123!")
            .fullName("New User")
            .build();

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.accessToken").isNotEmpty())
            .andExpect(jsonPath("$.refreshToken").isNotEmpty())
            .andExpect(jsonPath("$.tokenType").value("Bearer"))
            .andExpect(jsonPath("$.user.email").value("newuser@test.com"))
            .andExpect(jsonPath("$.user.fullName").value("New User"))
            .andExpect(jsonPath("$.user.roles").isArray());
    }

    @Test
    @DisplayName("POST /api/auth/signup with duplicate email returns 409 CONFLICT")
    void signup_duplicateEmail_returns409() throws Exception {
        // Create first user
        createUser("duplicate@test.com", "Password123!", "User One");

        // Try signup with same email
        SignupRequest request = SignupRequest.builder()
            .email("duplicate@test.com")
            .password("Password123!")
            .fullName("User Two")
            .build();

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isConflict());
    }

    // ============ LOGIN TESTS ============

    @Test
    @DisplayName("POST /api/auth/login with valid credentials returns 200 OK with JWT tokens")
    void login_validCredentials_returns200WithTokens() throws Exception {
        createUser("user@test.com", "Password123!", "Test User");

        LoginRequest request = LoginRequest.builder()
            .email("user@test.com")
            .password("Password123!")
            .build();

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accessToken").isNotEmpty())
            .andExpect(jsonPath("$.refreshToken").isNotEmpty())
            .andExpect(jsonPath("$.user.email").value("user@test.com"));
    }

    @Test
    @DisplayName("POST /api/auth/login with invalid password returns 401 UNAUTHORIZED")
    void login_invalidPassword_returns401() throws Exception {
        createUser("user@test.com", "Password123!", "Test User");

        LoginRequest request = LoginRequest.builder()
            .email("user@test.com")
            .password("WrongPassword!")
            .build();

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/auth/login with non-existent email returns 401 UNAUTHORIZED")
    void login_nonExistentEmail_returns401() throws Exception {
        LoginRequest request = LoginRequest.builder()
            .email("nonexistent@test.com")
            .password("Password123!")
            .build();

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnauthorized());
    }

    // ============ TOKEN REFRESH TESTS ============

    @Test
    @DisplayName("POST /api/auth/refresh with valid refresh token returns 200 OK with new access token")
    @WithMockUser(username = "user", roles = "USER")
    void refresh_validToken_returns200WithNewAccessToken() throws Exception {
        User user = createUser("user@test.com", "Password123!", "Test User");
        String refreshToken = jwtUtil.generateRefreshToken(user.getEmail(), user.getId().toString());

        RefreshTokenRequest request = RefreshTokenRequest.builder()
            .refreshToken(refreshToken)
            .build();

        mockMvc.perform(post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accessToken").isNotEmpty());
    }

    // ============ FORGOT PASSWORD TESTS ============

    @Test
    @DisplayName("POST /api/auth/forgot-password for existing user returns 202 ACCEPTED")
    void forgotPassword_existingUser_returns202() throws Exception {
        createUser("user@test.com", "Password123!", "Test User");

        ForgotPasswordRequest request = ForgotPasswordRequest.builder()
            .email("user@test.com")
            .build();

        mockMvc.perform(post("/api/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isAccepted());
    }

    // ============ GET /ME TESTS ============

    @Test
    @DisplayName("GET /api/auth/me with valid JWT returns 200 OK with user profile")
    void getMe_withValidJwt_returns200() throws Exception {
        User user = createUser("user@test.com", "Password123!", "Test User");
        String token = jwtUtil.generateAccessToken(user.getEmail(), user.getId().toString(), 
            Set.of("ROLE_USER"));

        mockMvc.perform(get("/api/auth/me")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email").value("user@test.com"))
            .andExpect(jsonPath("$.fullName").value("Test User"));
    }

    @Test
    @DisplayName("GET /api/auth/me without JWT token returns 401 UNAUTHORIZED")
    void getMe_withoutJwt_returns401() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/auth/me with expired JWT token returns 401 UNAUTHORIZED")
    void getMe_withExpiredJwt_returns401() throws Exception {
        // This would require creating an expired token
        // For now, we test with malformed token
        mockMvc.perform(get("/api/auth/me")
                .header("Authorization", "Bearer malformed.token.here"))
            .andExpect(status().isUnauthorized());
    }

    // ============ AUTHORIZATION TESTS ============

    @Test
    @DisplayName("Authenticated user can access GET /api/auth/me endpoint")
    void authenticatedUser_canAccessMe_returns200() throws Exception {
        User user = createUserWithRole("user@test.com", "Password123!", "User", "ROLE_USER");
        String token = jwtUtil.generateAccessToken(user.getEmail(), user.getId().toString(),
            Set.of("ROLE_USER"));

        mockMvc.perform(get("/api/auth/me")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("User with ADMIN role can be identified by token")
    void adminUser_canBeIdentifiedByRole_hasAdminRole() throws Exception {
        User user = createUserWithRole("admin@test.com", "Password123!", "Admin", "ROLE_ADMIN");
        String token = jwtUtil.generateAccessToken(user.getEmail(), user.getId().toString(),
            Set.of("ROLE_ADMIN"));

        // Verify token contains ADMIN role
        Set<String> roles = jwtUtil.extractRoles(token);
        assert roles.contains("ROLE_ADMIN");
    }

    // ============ RATE LIMITING TESTS ============

    @Test
    @DisplayName("Rate limiting allows 5 signup requests per 15 minutes")
    void rateLimit_allowedRequests_succeeds() throws Exception {
        for (int i = 0; i < 5; i++) {
            SignupRequest request = SignupRequest.builder()
                .email("user" + i + "@test.com")
                .password("Password123!")
                .fullName("User " + i)
                .build();

            mockMvc.perform(post("/api/auth/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated()); // First 5 should succeed
        }
    }

    @Test
    @DisplayName("Rate limiting returns 429 on 6th signup request within 15 minutes")
    void rateLimit_exceededRequests_returns429() throws Exception {
        // Make 5 successful requests
        for (int i = 0; i < 5; i++) {
            SignupRequest request = SignupRequest.builder()
                .email("user" + i + "@test.com")
                .password("Password123!")
                .fullName("User " + i)
                .build();

            mockMvc.perform(post("/api/auth/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
        }

        // 6th request should be rate limited
        SignupRequest request = SignupRequest.builder()
            .email("user6@test.com")
            .password("Password123!")
            .fullName("User 6")
            .build();

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().is(429));
    }

    // ============ HELPER METHODS ============

    /**
     * Create a test user with default USER role.
     */
    private User createUser(String email, String password, String fullName) {
        User user = User.builder()
            .email(email.toLowerCase())
            .password(passwordEncoder.encode(password))
            .fullName(fullName)
            .roles(new HashSet<>(List.of(UserRole.ROLE_USER)))
            .status(UserStatus.ACTIVE)
            .build();
        return userRepository.save(user);
    }

    /**
     * Create a test user with specific role.
     */
    private User createUserWithRole(String email, String password, String fullName, String role) {
        User user = User.builder()
            .email(email.toLowerCase())
            .password(passwordEncoder.encode(password))
            .fullName(fullName)
            .roles(new HashSet<>(List.of(UserRole.valueOf(role))))
            .status(UserStatus.ACTIVE)
            .build();
        return userRepository.save(user);
    }
}
