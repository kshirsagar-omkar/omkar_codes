package com.hoolistem.hoolistem.model.workshop;

import com.hoolistem.hoolistem.model.base.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Workshop entity representing a training/workshop offering.
 */
@Entity
@Table(name = "workshops")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Workshop extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "slug", nullable = false, unique = true, length = 255)
    private String slug;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "thumbnail_url", length = 500)
    private String thumbnailUrl;

    @Column(name = "default_fee", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal defaultFee = BigDecimal.ZERO;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    // ========================
    // RELATIONSHIPS
    // ========================

    @OneToMany(mappedBy = "workshop", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<WorkshopRegistration> registrations = new ArrayList<>();

    // ========================
    // HELPER METHODS
    // ========================

    public int getTotalRegistrations() {
        return registrations.size();
    }
}
