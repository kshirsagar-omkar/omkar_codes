package com.hoolistem.hoolistem.model.course;

import com.hoolistem.hoolistem.model.base.BaseAuditEntity;
import com.hoolistem.hoolistem.model.certificate.CourseCertificate;
import com.hoolistem.hoolistem.model.enums.CourseStatus;
import com.hoolistem.hoolistem.model.rating.CourseRating;
import com.hoolistem.hoolistem.model.user.InstructorDetail;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Course entity representing an educational course in the STEM platform.
 * 
 * Contains course metadata, pricing, and relationships to modules,
 * enrollments, purchases, and ratings.
 */
@Entity
@Table(name = "courses", indexes = {
    @Index(name = "idx_courses_instructor", columnList = "instructor_id"),
    @Index(name = "idx_courses_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Course extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instructor_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_courses_instructor"))
    private InstructorDetail instructor;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "slug", nullable = false, unique = true, length = 255)
    private String slug;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "short_description", length = 500)
    private String shortDescription;

    @Column(name = "thumbnail_url", length = 500)
    private String thumbnailUrl;

    @Column(name = "price", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal price = BigDecimal.ZERO;

    @Column(name = "discount_price", precision = 12, scale = 2)
    private BigDecimal discountPrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private CourseStatus status = CourseStatus.DRAFT;

    @Column(name = "is_featured", nullable = false)
    @Builder.Default
    private Boolean isFeatured = false;

    @Column(name = "published_at")
    private OffsetDateTime publishedAt;

    // ========================
    // RELATIONSHIPS
    // ========================

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("position ASC")
    @Builder.Default
    private List<CourseModule> modules = new ArrayList<>();

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<CourseEnrollment> enrollments = new ArrayList<>();

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<com.hoolistem.hoolistem.model.ecommerce.CoursePurchase> purchases = new ArrayList<>();

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<CourseRating> ratings = new ArrayList<>();

    @OneToOne(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private CourseCertificate certificate;

    // ========================
    // HELPER METHODS
    // ========================

    public BigDecimal getEffectivePrice() {
        return discountPrice != null ? discountPrice : price;
    }

    public boolean isPublished() {
        return status == CourseStatus.PUBLISHED;
    }

    public boolean isFree() {
        return price.compareTo(BigDecimal.ZERO) == 0;
    }

    public void publish() {
        this.status = CourseStatus.PUBLISHED;
        this.publishedAt = OffsetDateTime.now();
    }

    public void archive() {
        this.status = CourseStatus.ARCHIVED;
    }

    public int getTotalModules() {
        return modules.size();
    }

    public int getTotalLessons() {
        return modules.stream()
                .mapToInt(m -> m.getLessons().size())
                .sum();
    }
}
