package org.example.hvvs.model;

import jakarta.persistence.*;

import java.sql.Timestamp;

@Entity
@Table(name = "security_staff_profiles")
@NamedQueries({
        @NamedQuery(name = "SecurityStaffProfile.findAll", query = "SELECT s FROM SecurityStaffProfiles s"),
        @NamedQuery(name = "SecurityStaffProfile.findByUserId", query = "SELECT s FROM SecurityStaffProfiles s WHERE s.user_id = :user_id"),
        @NamedQuery(name = "SecurityStaffProfile.findByBadgeNumber", query = "SELECT s FROM SecurityStaffProfiles s WHERE s.badge_number = :badge_number"),
        @NamedQuery(name = "SecurityStaffProfile.findByShift", query = "SELECT s FROM SecurityStaffProfiles s WHERE s.shift = :shift")
})
public class SecurityStaffProfiles extends BaseEntity {
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private Users user_id;

    @Column(name = "badge_number", nullable = false, unique = true)
    private String badge_number;

    @Column(name = "shift", nullable = false)
    private String shift;

    @Column(name = "created_at", nullable = false)
    private Timestamp created_at;

    @Column(name = "updated_at", nullable = false)
    private Timestamp updated_at;

    public SecurityStaffProfiles() {
        super();
    }

    public SecurityStaffProfiles(Users user_id, String badge_number, String shift, Timestamp created_at, Timestamp updated_at) {
        this.user_id = user_id;
        this.badge_number = badge_number;
        this.shift = shift;
        this.created_at = created_at;
        this.updated_at = updated_at;
    }

    public Users getUserId() {
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

    public void setUserId(Users users_id) {
        this.user_id = users_id;
    }

    public void setBadgeNumber(String badge_number) {
        this.badge_number = badge_number;
    }

    public void setShift(String shift) {
        this.shift = shift;
    }

    public void setCreatedAt(Timestamp created_at) {
        this.created_at = created_at;
    }

    public void setUpdatedAt(Timestamp updated_at) {
        this.updated_at = updated_at;
    }
}
