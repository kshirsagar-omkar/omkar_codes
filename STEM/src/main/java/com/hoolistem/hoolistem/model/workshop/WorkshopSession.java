package com.hoolistem.hoolistem.model.workshop;

import com.hoolistem.hoolistem.model.base.BaseAuditEntity;
import com.hoolistem.hoolistem.model.enums.WorkshopMode;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Workshop session details including dates, mode, and fee.
 */
@Entity
@Table(name = "workshop_sessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkshopSession extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workshop_registration_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_workshop_session_reg"))
    private WorkshopRegistration registration;

    @Enumerated(EnumType.STRING)
    @Column(name = "mode", nullable = false, length = 20)
    private WorkshopMode mode;

    @Column(name = "location", length = 255)
    private String location;

    @Column(name = "meeting_link", length = 500)
    private String meetingLink;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;

    @Column(name = "fee", nullable = false, precision = 12, scale = 2)
    private BigDecimal fee;

    @Column(name = "max_participants")
    private Integer maxParticipants;

    // ========================
    // RELATIONSHIPS
    // ========================

    @OneToOne(mappedBy = "session", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private WorkshopEarning earning;

    // ========================
    // HELPER METHODS
    // ========================

    public boolean isOnline() {
        return mode == WorkshopMode.ONLINE;
    }

    public boolean isOffline() {
        return mode == WorkshopMode.OFFLINE;
    }

    public boolean isHybrid() {
        return mode == WorkshopMode.HYBRID;
    }

    public int getDurationDays() {
        return (int) (endDate.toEpochDay() - startDate.toEpochDay()) + 1;
    }
}
