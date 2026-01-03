package com.hoolistem.hoolistem.model.course;

import com.hoolistem.hoolistem.model.base.BaseAuditEntity;
import com.hoolistem.hoolistem.model.quiz.Quiz;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Course lesson representing an individual learning unit within a module.
 * 
 * Contains resources, optional quiz, Q&A, and progress tracking.
 */
@Entity
@Table(name = "course_lessons", 
       indexes = @Index(name = "idx_course_lessons_module", columnList = "module_id"),
       uniqueConstraints = @UniqueConstraint(
           name = "uk_course_lessons_position", 
           columnNames = {"module_id", "position"}
       ))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseLesson extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "module_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_course_lessons_module"))
    private CourseModule module;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "duration_seconds")
    private Integer durationSeconds;

    @Column(name = "position", nullable = false)
    @Builder.Default
    private Integer position = 0;

    @Column(name = "is_preview", nullable = false)
    @Builder.Default
    private Boolean isPreview = false;

    // ========================
    // RELATIONSHIPS
    // ========================

    @OneToMany(mappedBy = "lesson", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("position ASC")
    @Builder.Default
    private List<CourseResource> resources = new ArrayList<>();

    @OneToOne(mappedBy = "lesson", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Quiz quiz;

    @OneToMany(mappedBy = "lesson", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<LessonQuestion> questions = new ArrayList<>();

    @OneToMany(mappedBy = "lesson", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<LessonProgress> progressRecords = new ArrayList<>();

    // ========================
    // HELPER METHODS
    // ========================

    public String getFormattedDuration() {
        if (durationSeconds == null || durationSeconds == 0) {
            return "0:00";
        }
        int minutes = durationSeconds / 60;
        int seconds = durationSeconds % 60;
        return String.format("%d:%02d", minutes, seconds);
    }

    public boolean hasQuiz() {
        return quiz != null;
    }
}
