package org.example.hvvs.model;

import jakarta.persistence.*;

import java.sql.Timestamp;
import java.sql.Date;

@Entity
@Table(name = "visit_requests")
@NamedQueries({
        @NamedQuery(name = "VisitRequest.findAll", query = "SELECT v FROM VisitRequests v"),
        @NamedQuery(name = "VisitRequest.findByUserId", query = "SELECT v FROM VisitRequests v WHERE v.user_id = :user_id"),
        @NamedQuery(name = "VisitRequest.findByVisitDay", query = "SELECT v FROM VisitRequests v WHERE v.visit_day = :visit_day"),
        @NamedQuery(name = "VisitRequest.findByStatus", query = "SELECT v FROM VisitRequests v WHERE v.status = :status")
})
public class VisitRequests extends BaseEntity {
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private Users user_id;

    @Column(name = "verification_code", nullable = false)
    private String verification_code;

    @Column(name = "visit_day", nullable = false)
    private Date visit_day;

    @Column(name = "visitor_name", nullable = false)
    private String visitor_name;
    
    @Column(name = "visitor_identity", nullable = false)
    private String visitor_identity;

    @Column(name = "purpose", nullable = false)
    private String purpose;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private VisitStatus status;

    @Column(name = "remarks")
    private String remarks;

    @Column(name = "unit_number", nullable = false)
    private String unit_number;

    @Column(name = "created_at", nullable = false)
    private Timestamp created_at;

    @Column(name = "updated_at", nullable = false)
    private Timestamp updated_at;

    @PrePersist
    protected void onCreate() {
        Timestamp now = new Timestamp(System.currentTimeMillis());
        this.created_at = now;
        this.updated_at = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updated_at = new Timestamp(System.currentTimeMillis());
    }

    public VisitRequests() {
        super();
    }

    public VisitRequests(Users user_id, String verification_code, Date visit_day, String visitor_name, 
                         String visitor_identity, String purpose, VisitStatus status,
                         String remarks, String unit_number) {
        this.user_id = user_id;
        this.verification_code = verification_code;
        this.visit_day = visit_day;
        this.visitor_name = visitor_name;
        this.visitor_identity = visitor_identity;
        this.purpose = purpose;
        this.status = status;
        this.remarks = remarks;
        this.unit_number = unit_number;
    }

    public enum VisitStatus {
        PENDING, PROGRESS, COMPLETED, CANCELLED
    }

    public Users getUserId() {
        return user_id;
    }

    public String getVerificationCode() {
        return verification_code;
    }

    public Date getVisitDay() {
        return visit_day;
    }

    public String getVisitorName() {
        return visitor_name;
    }

    public String getVisitorIdentity() {
        return visitor_identity;
    }

    public String getPurpose() {
        return purpose;
    }

    public VisitStatus getStatus() {
        return status;
    }

    public String getRemarks() {
        return remarks;
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

    public void setUserId(Users user_id) {
        this.user_id = user_id;
    }

    public void setVerificationCode(String verification_code) {
        this.verification_code = verification_code;
    }

    public void setVisitDay(Date visit_day) {
        this.visit_day = visit_day;
    }

    public void setVisitorName(String visitor_name) {
        this.visitor_name = visitor_name;
    }

    public void setVisitorIdentity(String visitor_identity) {
        this.visitor_identity = visitor_identity;
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

    public void setStatus(VisitStatus status) {
        this.status = status;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public void setUnitNumber(String unit_number) {
        this.unit_number = unit_number;
    }
}
