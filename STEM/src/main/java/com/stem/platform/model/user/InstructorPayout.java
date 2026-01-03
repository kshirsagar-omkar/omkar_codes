package com.hoolistem.hoolistem.model.user;

import com.hoolistem.hoolistem.model.base.BaseAuditEntity;
import com.hoolistem.hoolistem.model.enums.PayoutStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Instructor payout representing a payment from the platform to an instructor.
 * 
 * Contains period information, amount, and status tracking.
 * Each payout can have multiple transaction attempts.
 */
@Entity
@Table(name = "instructor_payouts", indexes = {
    @Index(name = "idx_instructor_payouts_instructor", columnList = "instructor_id"),
    @Index(name = "idx_instructor_payouts_status", columnList = "status"),
    @Index(name = "idx_instructor_payouts_period", columnList = "period_start, period_end")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InstructorPayout extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instructor_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_instructor_payouts_instructor"))
    private InstructorDetail instructor;

    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "period_start", nullable = false)
    private LocalDate periodStart;

    @Column(name = "period_end", nullable = false)
    private LocalDate periodEnd;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private PayoutStatus status = PayoutStatus.PENDING;

    @Column(name = "reference_note", columnDefinition = "TEXT")
    private String referenceNote;

    @Column(name = "paid_at")
    private OffsetDateTime paidAt;

    // ========================
    // RELATIONSHIPS
    // ========================

    @OneToMany(mappedBy = "instructorPayout", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<PayoutTransaction> transactions = new ArrayList<>();

    // ========================
    // HELPER METHODS
    // ========================

    public void markAsCompleted() {
        this.status = PayoutStatus.COMPLETED;
        this.paidAt = OffsetDateTime.now();
    }

    public void markAsFailed() {
        this.status = PayoutStatus.FAILED;
    }
}
