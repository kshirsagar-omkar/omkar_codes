package com.hoolistem.hoolistem.model.rating;

import com.hoolistem.hoolistem.model.base.BaseAuditEntity;
import com.hoolistem.hoolistem.model.course.Course;
import com.hoolistem.hoolistem.model.user.User;
import jakarta.persistence.*;
import lombok.*;

/**
 * Course rating and review.
 * 
 * One rating per user per course (enforced via unique constraint).
 */
@Entity
@Table(name = "course_ratings",
       indexes = @Index(name = "idx_course_ratings_course", columnList = "course_id"),
       uniqueConstraints = @UniqueConstraint(
           name = "uk_course_ratings",
           columnNames = {"user_id", "course_id"}
       ))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseRating extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, length = 128,
                foreignKey = @ForeignKey(name = "fk_course_ratings_user"))
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_course_ratings_course"))
    private Course course;

    @Column(name = "rating", nullable = false)
    private Integer rating;

    @Column(name = "review", columnDefinition = "TEXT")
    private String review;

    @Column(name = "is_verified", nullable = false)
    @Builder.Default
    private Boolean isVerified = false;

    // ========================
    // HELPER METHODS
    // ========================

    @PrePersist
    @PreUpdate
    public void validateRating() {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
    }
}
