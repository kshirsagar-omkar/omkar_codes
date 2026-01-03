package com.hoolistem.hoolistem.model.user;

import com.hoolistem.hoolistem.model.base.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

/**
 * Instructor payment details for receiving payouts.
 * 
 * Supports both bank transfer and UPI payment methods.
 * Only one active payment detail per instructor (enforced via partial index in DB).
 */
@Entity
@Table(name = "instructor_payment_details")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InstructorPaymentDetail extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instructor_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_instructor_payment_instructor"))
    private InstructorDetail instructor;

    @Column(name = "account_holder_name", nullable = false, length = 255)
    private String accountHolderName;

    @Column(name = "bank_name", length = 255)
    private String bankName;

    @Column(name = "account_number", length = 50)
    private String accountNumber;

    @Column(name = "ifsc_code", length = 20)
    private String ifscCode;

    @Column(name = "upi_id", length = 100)
    private String upiId;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "is_verified", nullable = false)
    @Builder.Default
    private Boolean isVerified = false;

    @Column(name = "verified_at")
    private OffsetDateTime verifiedAt;

    // ========================
    // HELPER METHODS
    // ========================

    public void verify() {
        this.isVerified = true;
        this.verifiedAt = OffsetDateTime.now();
    }

    public void deactivate() {
        this.isActive = false;
    }

    public boolean hasValidBankDetails() {
        return bankName != null && accountNumber != null && ifscCode != null;
    }

    public boolean hasValidUpiDetails() {
        return upiId != null && !upiId.isBlank();
    }
}
