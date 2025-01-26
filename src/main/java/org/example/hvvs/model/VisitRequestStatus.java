package org.example.hvvs.model;

import jakarta.persistence.*;
import org.example.hvvs.commonClasses.BaseEntity;
import java.sql.Timestamp;

/**
 * Because there is exactly one status record per request,
 * we use a one-to-one mapping referencing the same primary key
 * or a dedicated foreign key column (request_id).
 */
@Entity
@Table(name = "visit_request_status")
public class VisitRequestStatus extends BaseEntity {

    /**
     * One-to-one link back to the request.
     * 'request_id' is a foreign key column referencing 'visit_request.id'.
     */
    @OneToOne
    @JoinColumn(name = "request_id", nullable = false)
    private VisitRequest request;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "remarks")
    private String remarks;

    @Column(name = "created_at", nullable = false)
    private Timestamp createdAt;

    @Column(name = "updated_at", nullable = false)
    private Timestamp updatedAt;

    public VisitRequestStatus() {}

    public VisitRequestStatus(VisitRequest request,
                              String status,
                              String remarks,
                              Timestamp createdAt,
                              Timestamp updatedAt) {
        this.request = request;
        this.status = status;
        this.remarks = remarks;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public VisitRequest getRequest() {
        return request;
    }

    public void setRequest(VisitRequest request) {
        this.request = request;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }
}
