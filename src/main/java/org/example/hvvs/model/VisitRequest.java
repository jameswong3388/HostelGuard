package org.example.hvvs.model;

import jakarta.persistence.*;
import org.example.hvvs.commonClasses.BaseEntity;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;

@Entity
@Table(name = "visit_request")
@NamedQueries({
        @NamedQuery(name = "VisitRequest.findAll", query = "SELECT v FROM VisitRequest v"),
        @NamedQuery(name = "VisitRequest.findByUserId", query = "SELECT v FROM VisitRequest v WHERE v.user_id = :user_id"),
        @NamedQuery(name = "VisitRequest.findBySerialNumber", query = "SELECT v FROM VisitRequest v WHERE v.serial_number = :serial_number"),
        @NamedQuery(name = "VisitRequest.findByVisitDate", query = "SELECT v FROM VisitRequest v WHERE v.visit_date = :visit_date")
})
public class VisitRequest extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user_id;

    @Column(name = "serial_number", nullable = false, unique = true)
    private String serial_number;

    @Column(name = "verification_code", nullable = false)
    private String verification_code;

    @Column(name = "visit_date", nullable = false)
    private Date visit_date;

    @Column(name = "visit_time", nullable = false)
    private Time visit_time;

    @Column(name = "purpose", nullable = false)
    private String purpose;

    @Column(name = "created_at", nullable = false)
    private Timestamp created_at;

    @Column(name = "updated_at", nullable = false)
    private Timestamp updated_at;

    public VisitRequest() {
        super();
    }

    public VisitRequest(User user_id, String serial_number, String verification_code, Date visit_date, 
                       Time visit_time, String purpose, Timestamp created_at, Timestamp updated_at) {
        this.user_id = user_id;
        this.serial_number = serial_number;
        this.verification_code = verification_code;
        this.visit_date = visit_date;
        this.visit_time = visit_time;
        this.purpose = purpose;
        this.created_at = created_at;
        this.updated_at = updated_at;
    }

    public User getUserId() {
        return user_id;
    }

    public String getSerialNumber() {
        return serial_number;
    }

    public String getVerificationCode() {
        return verification_code;
    }

    public Date getVisitDate() {
        return visit_date;
    }

    public Time getVisitTime() {
        return visit_time;
    }

    public String getPurpose() {
        return purpose;
    }

    public Timestamp getCreatedAt() {
        return created_at;
    }

    public Timestamp getUpdatedAt() {
        return updated_at;
    }
}
