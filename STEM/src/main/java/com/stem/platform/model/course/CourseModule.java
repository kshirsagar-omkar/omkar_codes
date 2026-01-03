package com.hoolistem.hoolistem.model.course;

import com.hoolistem.hoolistem.model.base.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Course module representing a section/chapter within a course.
 * 
 * Contains lessons organized by position.
 */
@Entity
@Table(name = "course_modules", 
       indexes = @Index(name = "idx_course_modules_course", columnList = "course_id"),
       uniqueConstraints = @UniqueConstraint(
           name = "uk_course_modules_position", 
           columnNames = {"course_id", "position"}
       ))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseModule extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_course_modules_course"))
    private Course course;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "position", nullable = false)
    @Builder.Default
    private Integer position = 0;

    @Column(name = "is_free", nullable = false)
    @Builder.Default
    private Boolean isFree = false;

    // ========================
    // RELATIONSHIPS
    // ========================

    @OneToMany(mappedBy = "module", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("position ASC")
    @Builder.Default
    private List<CourseLesson> lessons = new ArrayList<>();

    // ========================
    // HELPER METHODS
    // ========================

    public int getTotalLessons() {
        return lessons.size();
    }
}
