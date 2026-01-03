package com.hoolistem.hoolistem.model.ecommerce;

import com.hoolistem.hoolistem.model.base.BaseAuditEntity;
import com.hoolistem.hoolistem.model.course.Course;
import com.hoolistem.hoolistem.model.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Course purchase record linking user, course, and transaction.
 * 
 * Stores the price at time of purchase for historical accuracy.
 */
@Entity
@Table(name = "course_purchases",
       uniqueConstraints = @UniqueConstraint(
           name = "uk_course_purchase",
           columnNames = {"user_id", "course_id"}
       ))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CoursePurchase extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_course_purchase_user"))
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_course_purchase_course"))
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_course_purchase_tx"))
    private Transaction transaction;

    @Column(name = "price_paid", nullable = false, precision = 12, scale = 2)
    private BigDecimal pricePaid;

    @Column(name = "purchased_at", nullable = false)
    @Builder.Default
    private OffsetDateTime purchasedAt = OffsetDateTime.now();
}
