package com.hoolistem.hoolistem.model.user;

import com.hoolistem.hoolistem.model.base.BaseAuditEntity;
import com.hoolistem.hoolistem.model.certificate.IssuedCertificate;
import com.hoolistem.hoolistem.model.course.CourseEnrollment;
import com.hoolistem.hoolistem.model.course.LessonProgress;
import com.hoolistem.hoolistem.model.course.LessonQuestion;
import com.hoolistem.hoolistem.model.quiz.QuizAttempt;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Student-specific details extending user profile.
 * 
 * Contains student education information and relationships
 * to enrollments, progress tracking, quiz attempts, and certificates.
 */
@Entity
@Table(name = "student_details")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentDetail extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false, unique = true,
                foreignKey = @ForeignKey(name = "fk_student_details_profile"))
    private UserProfile profile;

    @Column(name = "education_level", length = 100)
    private String educationLevel;

    @Column(name = "institution", length = 255)
    private String institution;

    /**
     * Additional profile data stored as JSONB.
     * Can include: interests, learning goals, etc.
     */
    @Type(JsonType.class)
    @Column(name = "profile_data", columnDefinition = "jsonb")
    @Builder.Default
    private Map<String, Object> profileData = new HashMap<>();

    // ========================
    // RELATIONSHIPS
    // ========================

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<CourseEnrollment> enrollments = new ArrayList<>();

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<LessonProgress> lessonProgressList = new ArrayList<>();

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<QuizAttempt> quizAttempts = new ArrayList<>();

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<LessonQuestion> lessonQuestions = new ArrayList<>();

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<IssuedCertificate> certificates = new ArrayList<>();
}
