package com.hoolistem.hoolistem.model.workshop;

import com.hoolistem.hoolistem.model.base.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Workshop earning record for financial tracking.
 */
@Entity
@Table(name = "workshop_earnings",
       uniqueConstraints = @UniqueConstraint(
           name = "uk_workshop_earning",
           columnNames = {"workshop_session_id"}
       ))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkshopEarning extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workshop_session_id", nullable = false, unique = true,
                foreignKey = @ForeignKey(name = "fk_workshop_earning_session"))
    private WorkshopSession session;

    @Column(name = "total_participants", nullable = false)
    @Builder.Default
    private Integer totalParticipants = 0;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "recorded_at", nullable = false)
    @Builder.Default
    private OffsetDateTime recordedAt = OffsetDateTime.now();

    // ========================
    // HELPER METHODS
    // ========================

    public BigDecimal getAveragePerParticipant() {
        if (totalParticipants == 0) {
            return BigDecimal.ZERO;
        }
        return totalAmount.divide(BigDecimal.valueOf(totalParticipants), 2, java.math.RoundingMode.HALF_UP);
    }
}
