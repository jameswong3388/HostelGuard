package org.example.hvvs.model;

import jakarta.persistence.*;
import org.example.hvvs.commonClasses.BaseEntity;
import java.sql.Timestamp;

@Entity
@Table(name = "request_status")
@NamedQueries({
        @NamedQuery(name = "RequestStatus.findAll", query = "SELECT r FROM RequestStatus r"),
        @NamedQuery(name = "RequestStatus.findByRequestId", query = "SELECT r FROM RequestStatus r WHERE r.request_id = :request_id"),
        @NamedQuery(name = "RequestStatus.findByStatus", query = "SELECT r FROM RequestStatus r WHERE r.status = :status")
})
public class RequestStatus extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "request_id", nullable = false)
    private VisitRequest request_id;

    @ManyToOne
    @JoinColumn(name = "updated_by_user_id", nullable = false)
    private User updated_by_user_id;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "remarks")
    private String remarks;

    @Column(name = "created_at", nullable = false)
    private Timestamp created_at;

    @Column(name = "updated_at", nullable = false)
    private Timestamp updated_at;

    public RequestStatus() {
        super();
    }

    public RequestStatus(VisitRequest request_id, User updated_by_user_id, String status, String remarks, Timestamp created_at, Timestamp updated_at) {
        this.request_id = request_id;
        this.updated_by_user_id = updated_by_user_id;
        this.status = status;
        this.remarks = remarks;
        this.created_at = created_at;
        this.updated_at = updated_at;
    }

    public VisitRequest getRequestId() {
        return request_id;
    }

    public User getUpdatedByUserId() {
        return updated_by_user_id;
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
}
