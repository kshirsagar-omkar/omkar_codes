package com.hoolistem.hoolistem.model.enums;

/**
 * Enumeration of instructor payout statuses.
 * Maps to PostgreSQL ENUM type 'payout_status'.
 */
public enum PayoutStatus {
    PENDING,
    PROCESSING,
    COMPLETED,
    FAILED
}
