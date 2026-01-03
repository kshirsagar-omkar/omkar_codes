package com.hoolistem.hoolistem.model.ecommerce;

import com.hoolistem.hoolistem.model.base.BaseAuditEntity;
import com.hoolistem.hoolistem.model.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Shopping cart for a user.
 * 
 * Each user has exactly one cart (enforced via unique constraint).
 */
@Entity
@Table(name = "carts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cart extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true, length = 128,
                foreignKey = @ForeignKey(name = "fk_carts_user"))
    private User user;

    // ========================
    // RELATIONSHIPS
    // ========================

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<CartItem> items = new ArrayList<>();

    // ========================
    // HELPER METHODS
    // ========================

    public BigDecimal getTotalAmount() {
        return items.stream()
                .map(item -> item.getProduct().getEffectivePrice()
                        .multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public int getTotalItems() {
        return items.stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
    }

    public void clearCart() {
        items.clear();
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }
}
