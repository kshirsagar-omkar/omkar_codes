package com.hoolistem.hoolistem.model.ecommerce;

import com.hoolistem.hoolistem.model.base.BaseAuditEntity;
import com.hoolistem.hoolistem.model.rating.ProductRating;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Product entity representing physical or digital items for sale.
 */
@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "slug", nullable = false, unique = true, length = 255)
    private String slug;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "short_desc", length = 500)
    private String shortDesc;

    @Column(name = "price", nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(name = "discount_price", precision = 12, scale = 2)
    private BigDecimal discountPrice;

    @Column(name = "stock_quantity", nullable = false)
    @Builder.Default
    private Integer stockQuantity = 0;

    @Column(name = "sku", unique = true, length = 100)
    private String sku;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    // ========================
    // RELATIONSHIPS
    // ========================

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<CartItem> cartItems = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<OrderItem> orderItems = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ProductRating> ratings = new ArrayList<>();

    // ========================
    // HELPER METHODS
    // ========================

    public BigDecimal getEffectivePrice() {
        return discountPrice != null ? discountPrice : price;
    }

    public boolean isInStock() {
        return stockQuantity > 0;
    }

    public boolean hasStock(int quantity) {
        return stockQuantity >= quantity;
    }

    public void reduceStock(int quantity) {
        if (hasStock(quantity)) {
            this.stockQuantity -= quantity;
        } else {
            throw new IllegalStateException("Insufficient stock");
        }
    }

    public void increaseStock(int quantity) {
        this.stockQuantity += quantity;
    }
}
