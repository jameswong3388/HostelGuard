package org.example.hvvs.model;

import jakarta.persistence.*;
import org.example.hvvs.commonClasses.BaseEntity;
import java.sql.Timestamp;

@Entity
@Table(name = "security_staff_profile")
@NamedQueries({
        @NamedQuery(name = "SecurityStaffProfile.findAll", query = "SELECT s FROM SecurityStaffProfile s"),
        @NamedQuery(name = "SecurityStaffProfile.findByUserId", query = "SELECT s FROM SecurityStaffProfile s WHERE s.user_id = :user_id"),
        @NamedQuery(name = "SecurityStaffProfile.findByBadgeNumber", query = "SELECT s FROM SecurityStaffProfile s WHERE s.badge_number = :badge_number"),
        @NamedQuery(name = "SecurityStaffProfile.findByShift", query = "SELECT s FROM SecurityStaffProfile s WHERE s.shift = :shift")
})
public class SecurityStaffProfile extends BaseEntity {
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user_id;

    @Column(name = "badge_number", nullable = false, unique = true)
    private String badge_number;

    @Column(name = "shift", nullable = false)
    private String shift;

    @Column(name = "created_at", nullable = false)
    private Timestamp created_at;

    @Column(name = "updated_at", nullable = false)
    private Timestamp updated_at;

    public SecurityStaffProfile() {
        super();
    }

    public SecurityStaffProfile(User user_id, String badge_number, String shift, Timestamp created_at, Timestamp updated_at) {
        this.user_id = user_id;
        this.badge_number = badge_number;
        this.shift = shift;
        this.created_at = created_at;
        this.updated_at = updated_at;
    }

    public User getUserId() {
        return user_id;
    }

    public String getBadgeNumber() {
        return badge_number;
    }

    public String getShift() {
        return shift;
    }

    public Timestamp getCreatedAt() {
        return created_at;
    }

    public Timestamp getUpdatedAt() {
        return updated_at;
    }
}
