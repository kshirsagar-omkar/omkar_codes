package com.hoolistem.hoolistem.model.enums;

/**
 * Enumeration of workshop registration statuses.
 * Maps to PostgreSQL ENUM type 'registration_status'.
 */
public enum RegistrationStatus {
    PENDING,
    CONFIRMED,
    CANCELLED,
    COMPLETED
}
