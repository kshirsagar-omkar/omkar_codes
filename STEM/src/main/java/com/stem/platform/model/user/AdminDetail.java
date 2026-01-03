package com.hoolistem.hoolistem.model.user;

import com.hoolistem.hoolistem.model.base.BaseAuditEntity;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;

import java.util.HashMap;
import java.util.Map;

/**
 * Admin-specific details extending user profile.
 * 
 * Contains role-based permissions stored as JSONB for flexibility.
 */
@Entity
@Table(name = "admin_details")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminDetail extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false, unique = true,
                foreignKey = @ForeignKey(name = "fk_admin_details_profile"))
    private UserProfile profile;

    /**
     * Flexible permissions map stored as JSONB.
     * Example: {"manage_users": true, "manage_courses": true, "view_analytics": true}
     */
    @Type(JsonType.class)
    @Column(name = "permissions", columnDefinition = "jsonb", nullable = false)
    @Builder.Default
    private Map<String, Object> permissions = new HashMap<>();
}
