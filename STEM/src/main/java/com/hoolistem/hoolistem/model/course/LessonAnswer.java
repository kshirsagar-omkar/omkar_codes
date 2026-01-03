package com.hoolistem.hoolistem.model.course;

import com.hoolistem.hoolistem.model.base.BaseAuditEntity;
import com.hoolistem.hoolistem.model.user.InstructorDetail;
import jakarta.persistence.*;
import lombok.*;

/**
 * Answer provided by an instructor to a student's lesson question.
 * 
 * Part of the lesson Q&A feature for student-instructor interaction.
 */
@Entity
@Table(name = "lesson_answers", 
       indexes = @Index(name = "idx_lesson_answers_question", columnList = "question_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LessonAnswer extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_lesson_answers_question"))
    private LessonQuestion question;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instructor_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_lesson_answers_instructor"))
    private InstructorDetail instructor;

    @Column(name = "answer", nullable = false, columnDefinition = "TEXT")
    private String answer;

    @Column(name = "is_accepted", nullable = false)
    @Builder.Default
    private Boolean isAccepted = false;

    // ========================
    // HELPER METHODS
    // ========================

    public void accept() {
        this.isAccepted = true;
        // Also resolve the parent question
        if (this.question != null) {
            this.question.markAsResolved();
        }
    }
}
