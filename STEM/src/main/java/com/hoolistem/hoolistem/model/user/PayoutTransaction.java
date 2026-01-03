package com.hoolistem.hoolistem.model.user;

import com.hoolistem.hoolistem.model.base.BaseAuditEntity;
import com.hoolistem.hoolistem.model.enums.PayoutTransactionStatus;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Map;

/**
 * Individual payout transaction attempt.
 * 
 * Tracks gateway interactions for a payout, including
 * success/failure status and raw gateway response.
 */
@Entity
@Table(name = "payout_transactions", indexes = {
    @Index(name = "idx_payout_tx_payout", columnList = "instructor_payout_id"),
    @Index(name = "idx_payout_tx_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayoutTransaction extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instructor_payout_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_payout_tx_payout"))
    private InstructorPayout instructorPayout;

    @Column(name = "gateway_name", nullable = false, length = 100)
    private String gatewayName;

    @Column(name = "gateway_reference", length = 255)
    private String gatewayReference;

    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private PayoutTransactionStatus status = PayoutTransactionStatus.INITIATED;

    @Column(name = "failure_reason", columnDefinition = "TEXT")
    private String failureReason;

    @Column(name = "requested_at", nullable = false)
    @Builder.Default
    private OffsetDateTime requestedAt = OffsetDateTime.now();

    @Column(name = "processed_at")
    private OffsetDateTime processedAt;

    @Type(JsonType.class)
    @Column(name = "raw_response", columnDefinition = "jsonb")
    private Map<String, Object> rawResponse;

    // ========================
    // HELPER METHODS
    // ========================

    public void markAsSuccess(String gatewayReference, Map<String, Object> response) {
        this.status = PayoutTransactionStatus.SUCCESS;
        this.gatewayReference = gatewayReference;
        this.rawResponse = response;
        this.processedAt = OffsetDateTime.now();
    }

    public void markAsFailed(String reason, Map<String, Object> response) {
        this.status = PayoutTransactionStatus.FAILED;
        this.failureReason = reason;
        this.rawResponse = response;
        this.processedAt = OffsetDateTime.now();
    }
}
