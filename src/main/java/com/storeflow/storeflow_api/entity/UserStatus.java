package com.storeflow.storeflow_api.entity;

/**
 * User account status for lifecycle management.
 */
public enum UserStatus {
    ACTIVE,       // User account is active and can login
    INACTIVE,     // User account is inactive (suspended or disabled)
    SUSPENDED     // User account is suspended temporarily
}
