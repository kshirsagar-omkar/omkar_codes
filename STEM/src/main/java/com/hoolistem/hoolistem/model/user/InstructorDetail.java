package com.hoolistem.hoolistem.model.user;

import com.hoolistem.hoolistem.model.base.BaseAuditEntity;
import com.hoolistem.hoolistem.model.course.Course;
import com.hoolistem.hoolistem.model.course.LessonAnswer;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Instructor-specific details extending user profile.
 * 
 * Contains instructor qualifications, verification status, 
 * and relationships to courses, payment details, and payouts.
 */
@Entity
@Table(name = "instructor_details", indexes = {
    @Index(name = "idx_instructor_details_is_verified", columnList = "is_verified")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InstructorDetail extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false, unique = true,
                foreignKey = @ForeignKey(name = "fk_instructor_details_profile"))
    private UserProfile profile;

    @Column(name = "specialization", length = 255)
    private String specialization;

    @Column(name = "qualification", length = 255)
    private String qualification;

    @Column(name = "years_of_experience")
    @Builder.Default
    private Integer yearsOfExperience = 0;

    /**
     * Additional profile data stored as JSONB.
     * Can include: social links, certificates, awards, etc.
     */
    @Type(JsonType.class)
    @Column(name = "profile_data", columnDefinition = "jsonb")
    @Builder.Default
    private Map<String, Object> profileData = new HashMap<>();

    @Column(name = "is_verified", nullable = false)
    @Builder.Default
    private Boolean isVerified = false;

    @Column(name = "verified_at")
    private OffsetDateTime verifiedAt;

    // ========================
    // RELATIONSHIPS
    // ========================

    @OneToMany(mappedBy = "instructor", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Course> courses = new ArrayList<>();

    @OneToMany(mappedBy = "instructor", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<InstructorPaymentDetail> paymentDetails = new ArrayList<>();

    @OneToMany(mappedBy = "instructor", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<InstructorPayout> payouts = new ArrayList<>();

    @OneToMany(mappedBy = "instructor", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<LessonAnswer> lessonAnswers = new ArrayList<>();

    // ========================
    // HELPER METHODS
    // ========================

    public void verify() {
        this.isVerified = true;
        this.verifiedAt = OffsetDateTime.now();
    }
}
