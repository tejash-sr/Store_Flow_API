package com.storeflow.storeflow_api.repository;

import com.storeflow.storeflow_api.entity.User;
import com.storeflow.storeflow_api.entity.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for User entity data access operations.
 * Provides custom query methods for authentication and user lookup.
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * Find a user by email address (case-insensitive).
     * Used during login and email uniqueness validation.
     */
    Optional<User> findByEmailIgnoreCase(String email);

    /**
     * Find an active user by email (case-insensitive).
     * Used for authentication to ensure only active accounts can login.
     */
    Optional<User> findByEmailIgnoreCaseAndStatus(String email, UserStatus status);

    /**
     * Check if a user with given email already exists.
     * Used to prevent duplicate email registration.
     */
    boolean existsByEmailIgnoreCase(String email);

    /**
     * Find user by password reset token.
     * Used to validate reset token and retrieve associated user.
     */
    Optional<User> findByPasswordResetToken(String token);

    /**
     * Find all users with admin role.
     * Used to send admin notifications like daily digest emails.
     */
    @Query("SELECT u FROM User u WHERE :adminRole MEMBER OF u.roles")
    List<User> findAllAdmins();
}
