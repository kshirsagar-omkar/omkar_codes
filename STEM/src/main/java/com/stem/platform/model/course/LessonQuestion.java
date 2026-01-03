package com.hoolistem.hoolistem.model.course;

import com.hoolistem.hoolistem.model.base.BaseAuditEntity;
import com.hoolistem.hoolistem.model.user.StudentDetail;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Question asked by a student on a lesson.
 * 
 * Part of the lesson Q&A feature for student-instructor interaction.
 */
@Entity
@Table(name = "lesson_questions", 
       indexes = {
           @Index(name = "idx_lesson_questions_lesson", columnList = "lesson_id"),
           @Index(name = "idx_lesson_questions_student", columnList = "student_id")
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LessonQuestion extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_lesson_questions_lesson"))
    private CourseLesson lesson;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_lesson_questions_student"))
    private StudentDetail student;

    @Column(name = "title", length = 255)
    private String title;

    @Column(name = "question", nullable = false, columnDefinition = "TEXT")
    private String question;

    @Column(name = "is_resolved", nullable = false)
    @Builder.Default
    private Boolean isResolved = false;

    // ========================
    // RELATIONSHIPS
    // ========================

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<LessonAnswer> answers = new ArrayList<>();

    // ========================
    // HELPER METHODS
    // ========================

    public void markAsResolved() {
        this.isResolved = true;
    }

    public boolean hasAnswers() {
        return !answers.isEmpty();
    }
}
