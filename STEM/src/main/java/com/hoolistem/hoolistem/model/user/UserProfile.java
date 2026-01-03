package com.hoolistem.hoolistem.model.user;

import com.hoolistem.hoolistem.model.base.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * User profile containing personal information.
 * 
 * Has a 1:1 relationship with {@link User}.
 * 
 * Based on the user's role, exactly one of the following will be populated:
 * - {@link AdminDetail}
 * - {@link InstructorDetail}
 * - {@link StudentDetail}
 */
@Entity
@Table(name = "user_profiles", indexes = {
    @Index(name = "idx_user_profiles_user_id", columnList = "user_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfile extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true,
                foreignKey = @ForeignKey(name = "fk_user_profiles_user"))
    private User user;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "phone", unique = true, length = 20)
    private String phone;

    @Column(name = "bio", columnDefinition = "TEXT")
    private String bio;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    // ========================
    // ROLE-SPECIFIC DETAILS
    // ========================

    @OneToOne(mappedBy = "profile", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private AdminDetail adminDetail;

    @OneToOne(mappedBy = "profile", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private InstructorDetail instructorDetail;

    @OneToOne(mappedBy = "profile", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private StudentDetail studentDetail;

    // ========================
    // HELPER METHODS
    // ========================

    public String getFullName() {
        return firstName + " " + lastName;
    }
}
