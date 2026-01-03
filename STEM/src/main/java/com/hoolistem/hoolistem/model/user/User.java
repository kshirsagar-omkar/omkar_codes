package com.hoolistem.hoolistem.model.user;

import com.hoolistem.hoolistem.model.base.BaseAuditEntity;
import com.hoolistem.hoolistem.model.enums.UserRole;
import com.hoolistem.hoolistem.model.enums.UserStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * User entity representing authenticated users in the STEM platform.
 * 
 * Each user has a single profile ({@link UserProfile}) and can have multiple
 * addresses, carts, orders, transactions, and notifications.
 * 
 * The user's role determines which detail entity is populated:
 * - ADMIN → {@link AdminDetail}
 * - INSTRUCTOR → {@link InstructorDetail}
 * - STUDENT → {@link StudentDetail}
 */
@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_users_email", columnList = "email"),
    @Index(name = "idx_users_role", columnList = "role"),
    @Index(name = "idx_users_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseAuditEntity {

    /**
     * Firebase UID - provided by Firebase Authentication.
     * Not auto-generated, must be set from Firebase token.
     */
    @Id
    @Column(name = "id", nullable = false, length = 128)
    private String id;

    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    @Builder.Default
    private UserRole role = UserRole.STUDENT;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private UserStatus status = UserStatus.PENDING_VERIFICATION;

    @Column(name = "email_verified_at")
    private OffsetDateTime emailVerifiedAt;

    // ========================
    // RELATIONSHIPS
    // ========================

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private UserProfile profile;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private com.hoolistem.hoolistem.model.ecommerce.Cart cart;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<com.hoolistem.hoolistem.model.address.UserAddress> addresses = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<com.hoolistem.hoolistem.model.ecommerce.Transaction> transactions = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<com.hoolistem.hoolistem.model.ecommerce.Order> orders = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<com.hoolistem.hoolistem.model.notification.Notification> notifications = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<com.hoolistem.hoolistem.model.rating.CourseRating> courseRatings = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<com.hoolistem.hoolistem.model.rating.ProductRating> productRatings = new ArrayList<>();

    // ========================
    // HELPER METHODS
    // ========================

    public boolean isVerified() {
        return emailVerifiedAt != null;
    }

    public boolean isActive() {
        return status == UserStatus.ACTIVE;
    }

    public boolean isAdmin() {
        return role == UserRole.ADMIN;
    }

    public boolean isInstructor() {
        return role == UserRole.INSTRUCTOR;
    }

    public boolean isStudent() {
        return role == UserRole.STUDENT;
    }
}
