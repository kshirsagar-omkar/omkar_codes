package com.hoolistem.hoolistem.model.ecommerce;

import com.hoolistem.hoolistem.model.address.OrderAddress;
import com.hoolistem.hoolistem.model.base.BaseAuditEntity;
import com.hoolistem.hoolistem.model.enums.OrderStatus;
import com.hoolistem.hoolistem.model.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Order entity representing a product purchase.
 * 
 * Contains order items, total amounts, and delivery address.
 */
@Entity
@Table(name = "orders",
       indexes = {
           @Index(name = "idx_orders_user", columnList = "user_id"),
           @Index(name = "idx_orders_status", columnList = "status")
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_orders_user"))
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id",
                foreignKey = @ForeignKey(name = "fk_orders_transaction"))
    private Transaction transaction;

    @Column(name = "order_number", nullable = false, unique = true, length = 50)
    private String orderNumber;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "discount_amount", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "tax_amount", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private OrderStatus status = OrderStatus.PENDING;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    // ========================
    // RELATIONSHIPS
    // ========================

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private OrderAddress shippingAddress;

    // ========================
    // HELPER METHODS
    // ========================

    public BigDecimal getGrandTotal() {
        return totalAmount.subtract(discountAmount).add(taxAmount);
    }

    public void confirm() {
        this.status = OrderStatus.CONFIRMED;
    }

    public void ship() {
        this.status = OrderStatus.SHIPPED;
    }

    public void deliver() {
        this.status = OrderStatus.DELIVERED;
    }

    public void cancel() {
        this.status = OrderStatus.CANCELLED;
    }

    public boolean isCancellable() {
        return status == OrderStatus.PENDING || status == OrderStatus.CONFIRMED;
    }

    public int getTotalItems() {
        return items.stream().mapToInt(OrderItem::getQuantity).sum();
    }
}
