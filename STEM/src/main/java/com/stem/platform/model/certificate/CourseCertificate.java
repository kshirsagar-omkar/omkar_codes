package com.hoolistem.hoolistem.model.certificate;

import com.hoolistem.hoolistem.model.base.BaseAuditEntity;
import com.hoolistem.hoolistem.model.course.Course;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Course certificate template.
 * 
 * Each course can have one certificate template that is issued
 * to students upon course completion.
 */
@Entity
@Table(name = "course_certificates")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseCertificate extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false, unique = true,
                foreignKey = @ForeignKey(name = "fk_certificates_course"))
    private Course course;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "template_url", length = 500)
    private String templateUrl;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    // ========================
    // RELATIONSHIPS
    // ========================

    @OneToMany(mappedBy = "certificate", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<IssuedCertificate> issuedCertificates = new ArrayList<>();

    // ========================
    // HELPER METHODS
    // ========================

    public int getTotalIssued() {
        return issuedCertificates.size();
    }
}
