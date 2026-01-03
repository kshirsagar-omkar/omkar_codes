package com.hoolistem.hoolistem.model.quiz;

import com.hoolistem.hoolistem.model.base.BaseAuditEntity;
import com.hoolistem.hoolistem.model.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Quiz attempt representing a user's submission for a quiz.
 * 
 * Tracks score, time taken, pass/fail status, and all responses.
 */
@Entity
@Table(name = "quiz_attempts",
       indexes = {
           @Index(name = "idx_quiz_attempts_quiz", columnList = "quiz_id"),
           @Index(name = "idx_quiz_attempts_user", columnList = "user_id")
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizAttempt extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_quiz_attempts_quiz"))
    private Quiz quiz;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_quiz_attempts_user"))
    private User user;

    @Column(name = "score", nullable = false)
    @Builder.Default
    private Integer score = 0;

    @Column(name = "total_points", nullable = false)
    private Integer totalPoints;

    @Column(name = "percentage", nullable = false, precision = 5, scale = 2)
    private BigDecimal percentage;

    @Column(name = "passed", nullable = false)
    @Builder.Default
    private Boolean passed = false;

    @Column(name = "time_taken_secs")
    private Integer timeTakenSecs;

    @Column(name = "started_at", nullable = false)
    @Builder.Default
    private OffsetDateTime startedAt = OffsetDateTime.now();

    @Column(name = "completed_at")
    private OffsetDateTime completedAt;

    // ========================
    // RELATIONSHIPS
    // ========================

    @OneToMany(mappedBy = "attempt", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<QuizAttemptResponse> responses = new ArrayList<>();

    // ========================
    // HELPER METHODS
    // ========================

    public void complete() {
        this.completedAt = OffsetDateTime.now();
        if (this.startedAt != null) {
            long seconds = java.time.Duration.between(startedAt, completedAt).getSeconds();
            this.timeTakenSecs = (int) seconds;
        }
    }

    public void calculateScore() {
        int earnedPoints = responses.stream()
                .filter(r -> Boolean.TRUE.equals(r.getIsCorrect()))
                .mapToInt(r -> r.getQuestion().getPoints())
                .sum();
        
        this.score = earnedPoints;
        
        if (totalPoints != null && totalPoints > 0) {
            this.percentage = BigDecimal.valueOf(earnedPoints * 100.0 / totalPoints)
                    .setScale(2, java.math.RoundingMode.HALF_UP);
            this.passed = this.percentage.intValue() >= quiz.getPassingScore();
        }
    }

    public boolean isComplete() {
        return completedAt != null;
    }

    public String getFormattedTime() {
        if (timeTakenSecs == null) return "N/A";
        int minutes = timeTakenSecs / 60;
        int seconds = timeTakenSecs % 60;
        return String.format("%d:%02d", minutes, seconds);
    }
}
