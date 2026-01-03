package com.hoolistem.hoolistem.model.quiz;

import com.hoolistem.hoolistem.model.base.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Quiz question within a quiz assessment.
 * 
 * Contains the question text, explanation, points, and multiple choice options.
 */
@Entity
@Table(name = "quiz_questions",
       uniqueConstraints = @UniqueConstraint(
           name = "uk_quiz_questions_position",
           columnNames = {"quiz_id", "position"}
       ))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizQuestion extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_quiz_questions_quiz"))
    private Quiz quiz;

    @Column(name = "question_text", nullable = false, columnDefinition = "TEXT")
    private String questionText;

    @Column(name = "explanation", columnDefinition = "TEXT")
    private String explanation;

    @Column(name = "points", nullable = false)
    @Builder.Default
    private Integer points = 1;

    @Column(name = "position", nullable = false)
    @Builder.Default
    private Integer position = 0;

    // ========================
    // RELATIONSHIPS
    // ========================

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("position ASC")
    @Builder.Default
    private List<QuizOption> options = new ArrayList<>();

    // ========================
    // HELPER METHODS
    // ========================

    public QuizOption getCorrectOption() {
        return options.stream()
                .filter(QuizOption::getIsCorrect)
                .findFirst()
                .orElse(null);
    }

    public boolean hasMultipleCorrectOptions() {
        return options.stream()
                .filter(QuizOption::getIsCorrect)
                .count() > 1;
    }
}
