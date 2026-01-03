package com.hoolistem.hoolistem.model.enums;

/**
 * Enumeration of payout transaction statuses.
 * Maps to PostgreSQL ENUM type 'payout_tx_status'.
 */
public enum PayoutTransactionStatus {
    INITIATED,
    SUCCESS,
    FAILED
}
