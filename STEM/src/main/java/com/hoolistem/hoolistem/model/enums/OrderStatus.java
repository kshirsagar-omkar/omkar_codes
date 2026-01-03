package com.hoolistem.hoolistem.model.enums;

/**
 * Enumeration of order statuses.
 * Maps to PostgreSQL ENUM type 'order_status'.
 */
public enum OrderStatus {
    PENDING,
    CONFIRMED,
    PROCESSING,
    SHIPPED,
    DELIVERED,
    CANCELLED,
    RETURNED
}
