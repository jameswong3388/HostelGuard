package org.example.hvvs.model;

import jakarta.persistence.*;

import java.sql.Timestamp;

@Entity
@Table(name = "managing_staff_profiles")
@NamedQueries({
        @NamedQuery(name = "ManagingStaffProfile.findAll", query = "SELECT m FROM ManagingStaffProfiles m"),
        @NamedQuery(name = "ManagingStaffProfile.findByUserId", query = "SELECT m FROM ManagingStaffProfiles m WHERE m.user_id = :user_id"),
        @NamedQuery(name = "ManagingStaffProfile.findByDepartment", query = "SELECT m FROM ManagingStaffProfiles m WHERE m.department = :department"),
        @NamedQuery(name = "ManagingStaffProfile.findByPosition", query = "SELECT m FROM ManagingStaffProfiles m WHERE m.position = :position")
})
public class ManagingStaffProfiles extends BaseEntity {
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private Users user_id;

    @Column(name = "department", nullable = false)
    private String department;

    @Column(name = "position", nullable = false)
    private String position;

    @Column(name = "created_at", nullable = false)
    private Timestamp created_at;

    @Column(name = "updated_at", nullable = false)
    private Timestamp updated_at;

    public ManagingStaffProfiles() {
        super();
    }

    public ManagingStaffProfiles(Users user_id, String department, String position, Timestamp created_at, Timestamp updated_at) {
        this.user_id = user_id;
        this.department = department;
        this.position = position;
        this.created_at = created_at;
        this.updated_at = updated_at;
    }

    public Users getUserId() {
        return user_id;
    }

    public String getDepartment() {
        return department;
    }

    public String getPosition() {
        return position;
    }

    public Timestamp getCreatedAt() {
        return created_at;
    }

    public Timestamp getUpdatedAt() {
        return updated_at;
    }

    public void setUserId(Users user_id) {
        this.user_id = user_id;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public void setCreatedAt(Timestamp created_at) {
        this.created_at = created_at;
    }

    public void setUpdatedAt(Timestamp updated_at) {
        this.updated_at = updated_at;
    }
} 