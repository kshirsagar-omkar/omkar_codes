package com.hoolistem.hoolistem.model.rating;

import com.hoolistem.hoolistem.model.base.BaseAuditEntity;
import com.hoolistem.hoolistem.model.ecommerce.Product;
import com.hoolistem.hoolistem.model.user.User;
import jakarta.persistence.*;
import lombok.*;

/**
 * Product rating and review.
 * 
 * One rating per user per product (enforced via unique constraint).
 */
@Entity
@Table(name = "product_ratings",
       indexes = @Index(name = "idx_product_ratings_product", columnList = "product_id"),
       uniqueConstraints = @UniqueConstraint(
           name = "uk_product_ratings",
           columnNames = {"user_id", "product_id"}
       ))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductRating extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, length = 128,
                foreignKey = @ForeignKey(name = "fk_product_ratings_user"))
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_product_ratings_product"))
    private Product product;

    @Column(name = "rating", nullable = false)
    private Integer rating;

    @Column(name = "review", columnDefinition = "TEXT")
    private String review;

    @Column(name = "is_verified", nullable = false)
    @Builder.Default
    private Boolean isVerified = false;

    // ========================
    // HELPER METHODS
    // ========================

    @PrePersist
    @PreUpdate
    public void validateRating() {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
    }
}
