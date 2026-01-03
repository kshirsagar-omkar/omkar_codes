package com.hoolistem.hoolistem.model.course;

import com.hoolistem.hoolistem.model.base.BaseAuditEntity;
import com.hoolistem.hoolistem.model.enums.ResourceType;
import jakarta.persistence.*;
import lombok.*;

/**
 * Course resource representing learning materials attached to a lesson.
 * 
 * Can be videos, PDFs, documents, links, or images.
 */
@Entity
@Table(name = "course_resources", 
       indexes = @Index(name = "idx_course_resources_lesson", columnList = "lesson_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseResource extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_course_resources_lesson"))
    private CourseLesson lesson;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private ResourceType type;

    @Column(name = "title", length = 255)
    private String title;

    @Column(name = "video_key", length = 500)
    private String videoKey;

    @Column(name = "url", length = 1000)
    private String url;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "position", nullable = false)
    @Builder.Default
    private Integer position = 0;

    // ========================
    // HELPER METHODS
    // ========================

    public boolean isVideo() {
        return type == ResourceType.VIDEO;
    }

    public boolean isDocument() {
        return type == ResourceType.PDF || type == ResourceType.DOCUMENT;
    }

    public String getFormattedFileSize() {
        if (fileSize == null || fileSize == 0) {
            return "Unknown";
        }
        if (fileSize < 1024) {
            return fileSize + " B";
        } else if (fileSize < 1024 * 1024) {
            return String.format("%.1f KB", fileSize / 1024.0);
        } else {
            return String.format("%.1f MB", fileSize / (1024.0 * 1024));
        }
    }
}
