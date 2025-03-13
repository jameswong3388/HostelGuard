package org.example.hvvs.model;

import jakarta.persistence.*;

import java.sql.Timestamp;

@Entity
@Table(name = "audit_logs")
@NamedQueries({
        @NamedQuery(name = "AuditLog.findAll", query = "SELECT a FROM AuditLogs a"),
        @NamedQuery(name = "AuditLog.findByUserId", query = "SELECT a FROM AuditLogs a WHERE a.user_id = :userId"),
        @NamedQuery(name = "AuditLog.findByAction", query = "SELECT a FROM AuditLogs a WHERE a.action = :action"),
        @NamedQuery(name = "AuditLog.findByEntityType", query = "SELECT a FROM AuditLogs a WHERE a.entity_type = :entityType"),
        @NamedQuery(name = "AuditLog.findByEntityId", query = "SELECT a FROM AuditLogs a WHERE a.entity_id = :entityId"),
        @NamedQuery(name = "AuditLog.findByIpAddress", query = "SELECT a FROM AuditLogs a WHERE a.ip_address = :ipAddress"),
        @NamedQuery(name = "AuditLog.findByCreatedAt", query = "SELECT a FROM AuditLogs a WHERE a.created_at = :createdAt")
})
public class AuditLogs extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "user_id")
    private Users user_id;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false, length = 20)
    private Action action;

    @Column(name = "entity_type", nullable = false, length = 50)
    private String entity_type;

    @Column(name = "entity_id", length = 36)
    private String entity_id;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "ip_address", length = 45)
    private String ip_address;

    @Column(name = "user_agent", length = 512)
    private String user_agent;

    @Column(name = "old_values", columnDefinition = "JSON")
    private String old_values;

    @Column(name = "new_values", columnDefinition = "JSON")
    private String new_values;

    @Column(name = "additional_data", columnDefinition = "JSON")
    private String additional_data;

    @Column(name = "created_at", nullable = false)
    private Timestamp created_at;

    public enum Action {
        CREATE,
        READ,
        UPDATE,
        DELETE,
        LOGIN,
        LOGOUT,
    }

    public AuditLogs() {
        super();
    }

    public AuditLogs(Users user_id, Action action, String entity_type, String entity_id,
                     String description, String ip_address, String user_agent,
                     String old_values, String new_values, String additional_data) {
        this.user_id = user_id;
        this.action = action;
        this.entity_type = entity_type;
        this.entity_id = entity_id;
        this.description = description;
        this.ip_address = ip_address;
        this.user_agent = user_agent;
        this.old_values = old_values;
        this.new_values = new_values;
        this.additional_data = additional_data;
    }

    @PrePersist
    protected void onCreate() {
        this.created_at = new Timestamp(System.currentTimeMillis());
    }

    // Getters and Setters
    public Users getUserId() {
        return user_id;
    }

    public void setUserId(Users user_id) {
        this.user_id = user_id;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public String getEntityType() {
        return entity_type;
    }

    public void setEntityType(String entity_type) {
        this.entity_type = entity_type;
    }

    public String getEntityId() {
        return entity_id;
    }

    public void setEntityId(String entity_id) {
        this.entity_id = entity_id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIpAddress() {
        return ip_address;
    }

    public void setIpAddress(String ip_address) {
        this.ip_address = ip_address;
    }

    public String getUserAgent() {
        return user_agent;
    }

    public void setUserAgent(String user_agent) {
        this.user_agent = user_agent;
    }

    public String getOldValues() {
        return old_values;
    }

    public void setOldValues(String old_values) {
        this.old_values = old_values;
    }

    public String getNewValues() {
        return new_values;
    }

    public void setNewValues(String new_values) {
        this.new_values = new_values;
    }

    public String getAdditionalData() {
        return additional_data;
    }

    public void setAdditionalData(String additional_data) {
        this.additional_data = additional_data;
    }

    public Timestamp getCreatedAt() {
        return created_at;
    }

    public void setCreatedAt(Timestamp created_at) {
        this.created_at = created_at;
    }
} 