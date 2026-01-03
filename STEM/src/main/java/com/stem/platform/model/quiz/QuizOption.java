package com.hoolistem.hoolistem.model.quiz;

import com.hoolistem.hoolistem.model.base.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * Quiz option representing a possible answer choice for a question.
 */
@Entity
@Table(name = "quiz_options",
       indexes = @Index(name = "idx_quiz_options_question", columnList = "question_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizOption extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_quiz_options_question"))
    private QuizQuestion question;

    @Column(name = "option_text", nullable = false, columnDefinition = "TEXT")
    private String optionText;

    @Column(name = "is_correct", nullable = false)
    @Builder.Default
    private Boolean isCorrect = false;

    @Column(name = "position", nullable = false)
    @Builder.Default
    private Integer position = 0;
}
