package com.hoolistem.hoolistem.model.ecommerce;

import com.hoolistem.hoolistem.model.base.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Order item representing a product line in an order.
 * 
 * Contains snapshot of product name and price at time of order.
 */
@Entity
@Table(name = "order_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_order_items_order"))
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_order_items_product"))
    private Product product;

    @Column(name = "product_name", nullable = false, length = 255)
    private String productName;

    @Column(name = "price", nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    // ========================
    // HELPER METHODS
    // ========================

    public BigDecimal getSubtotal() {
        return price.multiply(BigDecimal.valueOf(quantity));
    }

    /**
     * Snapshot product details at time of order creation
     */
    public static OrderItem fromCartItem(CartItem cartItem, Order order) {
        Product product = cartItem.getProduct();
        return OrderItem.builder()
                .order(order)
                .product(product)
                .productName(product.getName())
                .price(product.getEffectivePrice())
                .quantity(cartItem.getQuantity())
                .build();
    }
}
