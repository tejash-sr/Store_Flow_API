package com.storeflow.storeflow_api.entity;

/**
 * User roles for role-based access control.
 */
public enum UserRole {
    ROLE_USER,    // Standard user - can browse products, place orders
    ROLE_ADMIN    // Administrator - can manage products, view all orders, user management
}
