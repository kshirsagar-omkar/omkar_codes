package com.hoolistem.hoolistem.model.course;

import com.hoolistem.hoolistem.model.base.BaseAuditEntity;
import com.hoolistem.hoolistem.model.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

/**
 * Course enrollment representing a user's access to a course.
 * 
 * Any user (Student, Instructor, Admin) can enroll in a course.
 * Tracks enrollment date and optional expiration for time-limited access.
 */
@Entity
@Table(name = "course_enrollments", 
       indexes = {
           @Index(name = "idx_enrollments_course", columnList = "course_id"),
           @Index(name = "idx_enrollments_user", columnList = "user_id")
       },
       uniqueConstraints = @UniqueConstraint(
           name = "uk_enrollments", 
           columnNames = {"course_id", "user_id"}
       ))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseEnrollment extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_enrollments_course"))
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_enrollments_student"))
    private StudentDetail student;

    @Column(name = "enrolled_at", nullable = false)
    @Builder.Default
    private OffsetDateTime enrolledAt = OffsetDateTime.now();

    @Column(name = "expires_at")
    private OffsetDateTime expiresAt;

    // ========================
    // HELPER METHODS
    // ========================

    public boolean isExpired() {
        if (expiresAt == null) {
            return false;
        }
        return OffsetDateTime.now().isAfter(expiresAt);
    }

    public boolean isActive() {
        return !isExpired();
    }
}
