package org.example.hvvs.model;

import jakarta.persistence.*;

import java.sql.Timestamp;

@Entity
@Table(name = "visit_requests")
@NamedQueries({
        @NamedQuery(name = "VisitRequest.findAll", query = "SELECT v FROM VisitRequests v"),
        @NamedQuery(name = "VisitRequest.findByUserId", query = "SELECT v FROM VisitRequests v WHERE v.user_id = :user_id"),
        @NamedQuery(name = "VisitRequest.findByVisitDate", query = "SELECT v FROM VisitRequests v WHERE v.visit_datetime = :visit_date"),
        @NamedQuery(name = "VisitRequest.findByStatus", query = "SELECT v FROM VisitRequests v WHERE v.status = :status")
})
public class VisitRequests extends BaseEntity {
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private Users user_id;

    @Column(name = "verification_code", nullable = false)
    private String verification_code;

    @Column(name = "visit_datetime", nullable = false)
    private Timestamp visit_datetime;

    @Column(name = "purpose", nullable = false)
    private String purpose;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private VisitStatus status;

    @Column(name = "remarks")
    private String remarks;

    @Column(name = "unit_number", nullable = false)
    private String unit_number;

    @Column(name = "number_of_entries", nullable = false)
    private int number_of_entries;

    @Column(name = "created_at", nullable = false)
    private Timestamp created_at;

    @Column(name = "updated_at", nullable = false)
    private Timestamp updated_at;

    public VisitRequests() {
        super();
    }

    public VisitRequests(Users user_id, String verification_code, Timestamp visit_datetime, String purpose, VisitStatus status,
                         String remarks, String unit_number, int number_of_entries, Timestamp created_at, Timestamp updated_at) {
        this.user_id = user_id;
        this.verification_code = verification_code;
        this.visit_datetime = visit_datetime;
        this.purpose = purpose;
        this.status = status;
        this.remarks = remarks;
        this.unit_number = unit_number;
        this.number_of_entries = number_of_entries;
        this.created_at = created_at;
        this.updated_at = updated_at;
    }

    public enum VisitStatus {
        PENDING, APPROVED, REJECTED, COMPLETED, PROGRESS, CANCELLED
    }

    public Users getUserId() {
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

    public VisitStatus getStatus() {
        return status;
    }

    public String getRemarks() {
        return remarks;
    }

    public String getUnitNumber() {
        return unit_number;
    }

    public int getNumberOfEntries() {
        return number_of_entries;
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

    public void setStatus(VisitStatus status) {
        this.status = status;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public void setUnitNumber(String unit_number) {
        this.unit_number = unit_number;
    }

    public void setNumberOfEntries(int number_of_entries) {
        this.number_of_entries = number_of_entries;
    }
}
