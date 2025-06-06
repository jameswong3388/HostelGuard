package org.example.hvvs.model;

import jakarta.persistence.*;

import java.sql.Timestamp;

@Entity
@Table(name = "visitor_records")
@NamedQueries({
        @NamedQuery(name = "VisitorRecord.findAll", query = "SELECT v FROM VisitorRecords v"),
        @NamedQuery(name = "VisitorRecord.findByRequestId", query = "SELECT v FROM VisitorRecords v WHERE v.request_id = :request_id"),
        @NamedQuery(name = "VisitorRecord.findBySecurityStaffId", query = "SELECT v FROM VisitorRecords v WHERE v.security_staff_id = :security_staff_id"),
        @NamedQuery(name = "VisitorRecord.findByVisitorName", query = "SELECT v FROM VisitorRecords v WHERE v.visitor_name = :visitor_name")
})
public class VisitorRecords extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "request_id", nullable = false)
    private VisitRequests request_id;

    @ManyToOne
    @JoinColumn(name = "security_staff_id", nullable = false)
    private Users security_staff_id;

    @Column(name = "visitor_name", nullable = false)
    private String visitor_name;

    @Column(name = "visitor_ic", nullable = false)
    private String visitor_ic;

    @Column(name = "visitor_phone", nullable = false)
    private String visitor_phone;

    @Column(name = "check_in_time")
    private Timestamp check_in_time;

    @Column(name = "check_out_time")
    private Timestamp check_out_time;

    @Column(name = "remarks")
    private String remarks;

    @Column(name = "created_at", nullable = false)
    private Timestamp created_at;

    @Column(name = "updated_at", nullable = false)
    private Timestamp updated_at;

    public VisitorRecords() {
        super();
    }

    public VisitorRecords(VisitRequests request_id, Users security_staff_id, String visitor_name,
                          String visitor_ic, String visitor_phone, Timestamp check_in_time, Timestamp check_out_time,
                          String remarks) {
        this.request_id = request_id;
        this.security_staff_id = security_staff_id;
        this.visitor_name = visitor_name;
        this.visitor_ic = visitor_ic;
        this.visitor_phone = visitor_phone;
        this.check_in_time = check_in_time;
        this.check_out_time = check_out_time;
        this.remarks = remarks;
    }

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

    public VisitRequests getRequestId() {
        return request_id;
    }

    public Users getSecurityStaffId() {
        return security_staff_id;
    }

    public String getVisitorName() {
        return visitor_name;
    }

    public String getVisitorIc() {
        return visitor_ic;
    }

    public String getVisitorPhone() {
        return visitor_phone;
    }

    public Timestamp getCheckInTime() {
        return check_in_time;
    }

    public Timestamp getCheckOutTime() {
        return check_out_time;
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

    public void setRequestId(VisitRequests request_id) {
        this.request_id = request_id;
    }

    public void setSecurityStaffId(Users security_staff_id) {
        this.security_staff_id = security_staff_id;
    }

    public void setVisitorName(String visitor_name) {
        this.visitor_name = visitor_name;
    }

    public void setVisitorIc(String visitor_ic) {
        this.visitor_ic = visitor_ic;
    }

    public void setVisitorPhone(String visitor_phone) {
        this.visitor_phone = visitor_phone;
    }

    public void setCheckInTime(Timestamp check_in_time) {
        this.check_in_time = check_in_time;
    }

    public void setCheckOutTime(Timestamp check_out_time) {
        this.check_out_time = check_out_time;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public void setCreatedAt(Timestamp created_at) {
        this.created_at = created_at;
    }

    public void setUpdatedAt(Timestamp updated_at) {
        this.updated_at = updated_at;
    }
}
