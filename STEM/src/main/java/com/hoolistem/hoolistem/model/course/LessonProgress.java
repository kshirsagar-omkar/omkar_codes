package com.hoolistem.hoolistem.model.course;

import com.hoolistem.hoolistem.model.base.BaseAuditEntity;
import com.hoolistem.hoolistem.model.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

/**
 * Lesson progress tracking for a user's learning journey.
 * 
 * Tracks completion percentage, video position, and completion status.
 */
@Entity
@Table(name = "lesson_progress", 
       indexes = @Index(name = "idx_progress_user", columnList = "user_id"),
       uniqueConstraints = @UniqueConstraint(
           name = "uk_progress", 
           columnNames = {"lesson_id", "user_id"}
       ))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LessonProgress extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_progress_lesson"))
    private CourseLesson lesson;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_progress_user"))
    private User user;

    @Column(name = "progress_pct", nullable = false)
    @Builder.Default
    private Integer progressPct = 0;

    @Column(name = "is_completed", nullable = false)
    @Builder.Default
    private Boolean isCompleted = false;

    @Column(name = "completed_at")
    private OffsetDateTime completedAt;

    @Column(name = "last_position")
    @Builder.Default
    private Integer lastPosition = 0;

    // ========================
    // HELPER METHODS
    // ========================

    public void updateProgress(int newProgress, int position) {
        this.progressPct = Math.min(100, Math.max(0, newProgress));
        this.lastPosition = position;
        
        if (this.progressPct >= 100 && !this.isCompleted) {
            markAsCompleted();
        }
    }

    public void markAsCompleted() {
        this.isCompleted = true;
        this.progressPct = 100;
        this.completedAt = OffsetDateTime.now();
    }
}
