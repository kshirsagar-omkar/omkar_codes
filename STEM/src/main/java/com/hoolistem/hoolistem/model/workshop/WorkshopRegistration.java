package com.hoolistem.hoolistem.model.workshop;

import com.hoolistem.hoolistem.model.base.BaseAuditEntity;
import com.hoolistem.hoolistem.model.enums.RegistrationStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

/**
 * Workshop registration from an individual or organization.
 * 
 * Does NOT require user login - guests can register directly.
 */
@Entity
@Table(name = "workshop_registrations",
       indexes = {
           @Index(name = "idx_workshop_reg_workshop", columnList = "workshop_id"),
           @Index(name = "idx_workshop_reg_email", columnList = "email")
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkshopRegistration extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workshop_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_workshop_reg_workshop"))
    private Workshop workshop;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "email", nullable = false, length = 255)
    private String email;

    @Column(name = "phone", nullable = false, length = 20)
    private String phone;

    @Column(name = "organization", length = 255)
    private String organization;

    @Column(name = "message", columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private RegistrationStatus status = RegistrationStatus.PENDING;

    @Column(name = "registered_at", nullable = false)
    @Builder.Default
    private OffsetDateTime registeredAt = OffsetDateTime.now();

    // ========================
    // RELATIONSHIPS
    // ========================

    @OneToOne(mappedBy = "registration", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private WorkshopSession session;

    // ========================
    // HELPER METHODS
    // ========================

    public void confirm() {
        this.status = RegistrationStatus.CONFIRMED;
    }

    public void cancel() {
        this.status = RegistrationStatus.CANCELLED;
    }

    public void complete() {
        this.status = RegistrationStatus.COMPLETED;
    }
}
