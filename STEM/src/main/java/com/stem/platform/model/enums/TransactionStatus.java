package com.hoolistem.hoolistem.model.enums;

/**
 * Enumeration of transaction statuses.
 * Maps to PostgreSQL ENUM type 'transaction_status'.
 */
public enum TransactionStatus {
    PENDING,
    SUCCESS,
    FAILED,
    REFUNDED
}
