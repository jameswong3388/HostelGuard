package org.example.hvvs.model;

import jakarta.persistence.*;
import org.example.hvvs.commonClasses.BaseEntity;

import java.sql.Timestamp;

@Entity
@Table(name = "visit_request")
@NamedQueries({
        @NamedQuery(name = "VisitRequest.findAll", query = "SELECT v FROM VisitRequest v"),
        @NamedQuery(name = "VisitRequest.findByUserId", query = "SELECT v FROM VisitRequest v WHERE v.user_id = :user_id"),
        @NamedQuery(name = "VisitRequest.findByVisitDate", query = "SELECT v FROM VisitRequest v WHERE v.visit_datetime = :visit_date")
})
public class VisitRequest extends BaseEntity {
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user_id;

    @Column(name = "verification_code", nullable = false)
    private String verification_code;

    @Column(name = "visit_datetime", nullable = false)
    private Timestamp visit_datetime;

    @Column(name = "purpose", nullable = false)
    private String purpose;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "remarks")
    private String remarks;

    @Column(name = "created_at", nullable = false)
    private Timestamp created_at;

    @Column(name = "updated_at", nullable = false)
    private Timestamp updated_at;

    public VisitRequest() {
        super();
    }

    public VisitRequest(User user_id, String verification_code, Timestamp visit_datetime, String purpose, String status,
                        String remarks, Timestamp created_at, Timestamp updated_at) {
        this.user_id = user_id;
        this.verification_code = verification_code;
        this.visit_datetime = visit_datetime;
        this.purpose = purpose;
        this.status = status;
        this.remarks = remarks;
        this.created_at = created_at;
        this.updated_at = updated_at;
    }

    public User getUserId() {
        return user_id;
    }

    public String getVerificationCode() {
        return verification_code;
    }

    public Timestamp getVisitDateTime() {
        return visit_datetime;
    }

    public String getPurpose() {
        return purpose;
    }

    public String getStatus() {
        return status;
    }

    public String getRemarks() {
        return remarks;
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

    public void setVerificationCode(String verification_code) {
        this.verification_code = verification_code;
    }

    public void setVisitDateTime(Timestamp visit_date) {
        this.visit_datetime = visit_date;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public void setCreatedAt(Timestamp created_at) {
        this.created_at = created_at;
    }

    public void setUpdatedAt(Timestamp updated_at) {
        this.updated_at = updated_at;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}
