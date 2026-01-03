package com.hoolistem.hoolistem.model.ecommerce;

import com.hoolistem.hoolistem.model.base.BaseAuditEntity;
import com.hoolistem.hoolistem.model.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

/**
 * Payment attempt for a transaction.
 * 
 * A single transaction may have multiple payment attempts
 * (e.g., if first attempt fails).
 */
@Entity
@Table(name = "payments",
       indexes = @Index(name = "idx_payments_transaction", columnList = "transaction_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_payments_transaction"))
    private Transaction transaction;

    @Column(name = "payment_method", nullable = false, length = 50)
    private String paymentMethod;

    @Column(name = "provider_ref", length = 255)
    private String providerRef;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private PaymentStatus status = PaymentStatus.PENDING;

    @Column(name = "failure_reason", columnDefinition = "TEXT")
    private String failureReason;

    @Column(name = "paid_at")
    private OffsetDateTime paidAt;

    // ========================
    // HELPER METHODS
    // ========================

    public void markAsSuccess(String providerRef) {
        this.status = PaymentStatus.SUCCESS;
        this.providerRef = providerRef;
        this.paidAt = OffsetDateTime.now();
    }

    public void markAsFailed(String reason) {
        this.status = PaymentStatus.FAILED;
        this.failureReason = reason;
    }

    public void markAsRefunded() {
        this.status = PaymentStatus.REFUNDED;
    }

    public boolean isSuccessful() {
        return status == PaymentStatus.SUCCESS;
    }
}
