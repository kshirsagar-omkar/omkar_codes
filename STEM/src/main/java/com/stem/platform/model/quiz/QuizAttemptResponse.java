package com.hoolistem.hoolistem.model.quiz;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.io.Serializable;
import java.time.OffsetDateTime;

/**
 * Individual response in a quiz attempt.
 * 
 * Links the attempt, question, selected option, and correctness.
 * 
 * Note: Only has created_at (responses are immutable after submission)
 */
@Entity
@Table(name = "quiz_attempt_responses",
       uniqueConstraints = @UniqueConstraint(
           name = "uk_quiz_responses",
           columnNames = {"attempt_id", "question_id"}
       ))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizAttemptResponse implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attempt_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_quiz_responses_attempt"))
    private QuizAttempt attempt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_quiz_responses_question"))
    private QuizQuestion question;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "selected_option_id",
                foreignKey = @ForeignKey(name = "fk_quiz_responses_option"))
    private QuizOption selectedOption;

    @Column(name = "is_correct")
    private Boolean isCorrect;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    // ========================
    // HELPER METHODS
    // ========================

    @PrePersist
    public void evaluateCorrectness() {
        if (selectedOption != null) {
            this.isCorrect = selectedOption.getIsCorrect();
        } else {
            this.isCorrect = false;
        }
    }
}
