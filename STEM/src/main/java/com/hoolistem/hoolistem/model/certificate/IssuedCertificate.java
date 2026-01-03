package com.hoolistem.hoolistem.model.certificate;

import com.hoolistem.hoolistem.model.base.BaseAuditEntity;
import com.hoolistem.hoolistem.model.user.StudentDetail;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Issued certificate to a student for course completion.
 * 
 * Contains a unique verification code for authenticity.
 */
@Entity
@Table(name = "issued_certificates",
       indexes = @Index(name = "idx_issued_cert_student", columnList = "student_id"),
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_issued_cert", columnNames = {"certificate_id", "student_id"}),
           @UniqueConstraint(name = "uk_certificate_uid", columnNames = {"certificate_uid"})
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IssuedCertificate extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "certificate_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_issued_cert_cert"))
    private CourseCertificate certificate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_issued_cert_student"))
    private StudentDetail student;

    @Column(name = "certificate_uid", nullable = false, unique = true, length = 50)
    private String certificateUid;

    @Column(name = "issued_at", nullable = false)
    @Builder.Default
    private OffsetDateTime issuedAt = OffsetDateTime.now();

    // ========================
    // LIFECYCLE CALLBACKS
    // ========================

    @PrePersist
    public void generateUid() {
        if (certificateUid == null || certificateUid.isBlank()) {
            // Generate a unique certificate ID: STEM-XXXXX-XXXXX
            this.certificateUid = "STEM-" + 
                UUID.randomUUID().toString().substring(0, 5).toUpperCase() + "-" +
                UUID.randomUUID().toString().substring(0, 5).toUpperCase();
        }
    }

    // ========================
    // HELPER METHODS
    // ========================

    public String getVerificationUrl(String baseUrl) {
        return baseUrl + "/verify/" + certificateUid;
    }
}
