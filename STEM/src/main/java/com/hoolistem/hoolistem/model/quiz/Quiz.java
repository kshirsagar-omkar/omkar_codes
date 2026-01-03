package com.hoolistem.hoolistem.model.quiz;

import com.hoolistem.hoolistem.model.base.BaseAuditEntity;
import com.hoolistem.hoolistem.model.course.CourseLesson;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Quiz entity representing an assessment attached to a lesson.
 * 
 * Contains questions, configuration for attempts/time limits, and
 * tracks all student attempts.
 */
@Entity
@Table(name = "quizzes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Quiz extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id", nullable = false, unique = true,
                foreignKey = @ForeignKey(name = "fk_quizzes_lesson"))
    private CourseLesson lesson;

    @Column(name = "title", length = 255)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "passing_score", nullable = false)
    @Builder.Default
    private Integer passingScore = 70;

    @Column(name = "max_attempts", nullable = false)
    @Builder.Default
    private Integer maxAttempts = 3;

    @Column(name = "time_limit_mins")
    private Integer timeLimitMins;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    // ========================
    // RELATIONSHIPS
    // ========================

    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("position ASC")
    @Builder.Default
    private List<QuizQuestion> questions = new ArrayList<>();

    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<QuizAttempt> attempts = new ArrayList<>();

    // ========================
    // HELPER METHODS
    // ========================

    public int getTotalQuestions() {
        return questions.size();
    }

    public int getTotalPoints() {
        return questions.stream()
                .mapToInt(QuizQuestion::getPoints)
                .sum();
    }

    public boolean hasTimeLimit() {
        return timeLimitMins != null && timeLimitMins > 0;
    }
}
