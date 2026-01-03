package com.hoolistem.hoolistem.model.enums;

/**
 * Enumeration of course publication statuses.
 * Maps to PostgreSQL ENUM type 'course_status'.
 */
public enum CourseStatus {
    DRAFT,
    UNDER_REVIEW,
    PUBLISHED,
    ARCHIVED
}
