package com.storeflow.storeflow_api.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;
import java.util.*;

/**
 * User entity representing an authenticated system user.
 * Stores authentication credentials, roles, and user profile information.
 */
@Entity
@Table(name = "users", uniqueConstraints = {
    @UniqueConstraint(columnNames = "email", name = "uk_users_email")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 255)
    private String email;

    @JsonIgnore
    @Column(nullable = false, length = 255)
    private String password; // BCrypt hashed

    @Column(nullable = false, length = 255)
    private String fullName;

    @Column(length = 500)
    private String avatar; // File path or URL to avatar image

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    @Builder.Default
    private Set<UserRole> roles = new HashSet<>(List.of(UserRole.ROLE_USER));

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private UserStatus status = UserStatus.ACTIVE;

    @Column(name = "password_reset_token")
    private String passwordResetToken;

    @Column(name = "password_reset_token_expiry")
    private LocalDateTime passwordResetTokenExpiry;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Check if password reset token is still valid (not expired).
     */
    public boolean isResetTokenValid() {
        return passwordResetToken != null && 
               passwordResetTokenExpiry != null && 
               passwordResetTokenExpiry.isAfter(LocalDateTime.now());
    }

    /**
     * Check if user account is active and can authenticate.
     */
    public boolean isActive() {
        return status == UserStatus.ACTIVE;
    }

    /**
     * Check if user has admin role.
     */
    public boolean isAdmin() {
        return roles != null && roles.contains(UserRole.ROLE_ADMIN);
    }
}
