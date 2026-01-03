package com.hoolistem.hoolistem.model.ecommerce;

import com.hoolistem.hoolistem.model.base.BaseAuditEntity;
import com.hoolistem.hoolistem.model.enums.TransactionStatus;
import com.hoolistem.hoolistem.model.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Financial transaction representing a monetary exchange.
 * 
 * Can be linked to course purchases, orders, or both.
 */
@Entity
@Table(name = "transactions",
       indexes = {
           @Index(name = "idx_transactions_user", columnList = "user_id"),
           @Index(name = "idx_transactions_status", columnList = "status")
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_transactions_user"))
    private User user;

    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 3)
    @Builder.Default
    private String currency = "INR";

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private TransactionStatus status = TransactionStatus.PENDING;

    @Column(name = "transaction_ref", length = 100)
    private String transactionRef;

    // ========================
    // RELATIONSHIPS
    // ========================

    @OneToMany(mappedBy = "transaction", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Payment> payments = new ArrayList<>();

    @OneToMany(mappedBy = "transaction", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<CoursePurchase> coursePurchases = new ArrayList<>();

    @OneToMany(mappedBy = "transaction", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Order> orders = new ArrayList<>();

    // ========================
    // HELPER METHODS
    // ========================

    public void markAsSuccess() {
        this.status = TransactionStatus.SUCCESS;
    }

    public void markAsFailed() {
        this.status = TransactionStatus.FAILED;
    }

    public void markAsRefunded() {
        this.status = TransactionStatus.REFUNDED;
    }

    public boolean isSuccessful() {
        return status == TransactionStatus.SUCCESS;
    }
}
