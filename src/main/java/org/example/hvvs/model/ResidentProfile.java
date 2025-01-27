package org.example.hvvs.model;

import jakarta.persistence.*;
import org.example.hvvs.commonClasses.BaseEntity;
import java.sql.Timestamp;

@Entity
@Table(name = "resident_profile")
@NamedQueries({
        @NamedQuery(name = "ResidentProfile.findAll", query = "SELECT r FROM ResidentProfile r"),
        @NamedQuery(name = "ResidentProfile.findByUserId", query = "SELECT r FROM ResidentProfile r WHERE r.user_id = :user_id"),
        @NamedQuery(name = "ResidentProfile.findByRoomNumber", query = "SELECT r FROM ResidentProfile r WHERE r.unit_number = :room_number"),
})
public class ResidentProfile extends BaseEntity {
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user_id;

    @Column(name = "unit_number", nullable = false)
    private String unit_number;

    @Column(name = "created_at", nullable = false)
    private Timestamp created_at;

    @Column(name = "updated_at", nullable = false)
    private Timestamp updated_at;

    public ResidentProfile() {
        super();
    }

    public ResidentProfile(User user_id, String unit_number, Timestamp created_at, Timestamp updated_at) {
        this.user_id = user_id;
        this.unit_number = unit_number;
        this.created_at = created_at;
        this.updated_at = updated_at;
    }

    public User getUserId() {
        return user_id;
    }

    public String getUnitNumber() {
        return unit_number;
    }

    public Timestamp getCreatedAt() {
        return created_at;
    }

    public Timestamp getUpdatedAt() {
        return updated_at;
    }

    public void setUserId(User user_id) {
        this.user_id = user_id;
    }

    public void setUnitNumber(String room_number) {
        this.unit_number = unit_number;
    }

    public void setCreatedAt(Timestamp created_at) {
        this.created_at = created_at;
    }

    public void setUpdatedAt(Timestamp updated_at) {
        this.updated_at = updated_at;
    }
}
