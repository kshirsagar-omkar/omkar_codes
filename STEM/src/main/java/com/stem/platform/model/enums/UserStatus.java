package com.hoolistem.hoolistem.model.enums;

/**
 * Enumeration of user account statuses.
 * Maps to PostgreSQL ENUM type 'user_status'.
 */
public enum UserStatus {
    ACTIVE,
    INACTIVE,
    SUSPENDED,
    PENDING_VERIFICATION
}
