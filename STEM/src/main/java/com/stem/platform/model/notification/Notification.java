package com.hoolistem.hoolistem.model.notification;

import com.hoolistem.hoolistem.model.base.BaseAuditEntity;
import com.hoolistem.hoolistem.model.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

/**
 * Notification for user communication.
 * 
 * Supports different notification types with optional action URLs.
 */
@Entity
@Table(name = "notifications",
       indexes = {
           @Index(name = "idx_notifications_user", columnList = "user_id")
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, length = 128,
                foreignKey = @ForeignKey(name = "fk_notifications_user"))
    private User user;

    @Column(name = "type", nullable = false, length = 50)
    private String type;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(name = "action_url", length = 500)
    private String actionUrl;

    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private Boolean isRead = false;

    @Column(name = "read_at")
    private OffsetDateTime readAt;

    // ========================
    // HELPER METHODS
    // ========================

    public void markAsRead() {
        this.isRead = true;
        this.readAt = OffsetDateTime.now();
    }

    public void markAsUnread() {
        this.isRead = false;
        this.readAt = null;
    }

    // ========================
    // NOTIFICATION TYPES
    // ========================

    public static final String TYPE_SYSTEM = "SYSTEM";
    public static final String TYPE_COURSE = "COURSE";
    public static final String TYPE_ORDER = "ORDER";
    public static final String TYPE_PAYMENT = "PAYMENT";
    public static final String TYPE_ENROLLMENT = "ENROLLMENT";
    public static final String TYPE_CERTIFICATE = "CERTIFICATE";
    public static final String TYPE_QUIZ = "QUIZ";
}
