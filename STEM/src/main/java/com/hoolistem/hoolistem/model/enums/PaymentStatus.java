package com.hoolistem.hoolistem.model.enums;

/**
 * Enumeration of payment statuses.
 * Maps to PostgreSQL ENUM type 'payment_status'.
 */
public enum PaymentStatus {
    PENDING,
    SUCCESS,
    FAILED,
    REFUNDED
}
