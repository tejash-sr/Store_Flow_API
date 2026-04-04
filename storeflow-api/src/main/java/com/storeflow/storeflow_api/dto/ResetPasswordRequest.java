package com.storeflow.storeflow_api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for password reset request.
 * Used to complete the password reset flow with valid token.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResetPasswordRequest {
    private String token;      // Password reset token from email link
    private String newPassword;// New password to set
}
