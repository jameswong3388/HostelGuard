package org.example.hvvs.model;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Table(name = "mfa_methods")
public class MfaMethods {
    @Id
    @UuidGenerator
    @JdbcTypeCode(SqlTypes.CHAR) // Store UUID as string
    @Column(
            name = "id",
            columnDefinition = "CHAR(36)" // Match MySQL CHAR(36) format
    )
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MfaMethodType method;

    @Column(name = "secret")
    private String secret;

    @Column(columnDefinition = "JSON", name = "recovery_codes")
    private String recoveryCodes;

    @Column(name = "is_primary")
    private Boolean isPrimary = false;

    @Column(name = "is_enabled")
    private Boolean isEnabled = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Timestamp createdAt;

    @Column(name = "updated_at", nullable = false)
    private Timestamp updatedAt;

    public MfaMethods() {
        super();
    }

    public MfaMethods(Users user, MfaMethodType method, String secret, String recoveryCodes, 
                     Boolean isPrimary, Boolean isEnabled) {
        this.user = user;
        this.method = method;
        this.secret = secret;
        this.recoveryCodes = recoveryCodes;
        this.isPrimary = isPrimary != null ? isPrimary : false;
        this.isEnabled = isEnabled != null ? isEnabled : true;
    }

    @PrePersist
    protected void onCreate() {
        Timestamp now = new Timestamp(System.currentTimeMillis());
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = new Timestamp(System.currentTimeMillis());
    }

    public Users getUser() {
        return user;
    }

    public void setUser(Users user) {
        this.user = user;
    }

    public MfaMethodType getMethod() {
        return method;
    }

    public void setMethod(MfaMethodType method) {
        this.method = method;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getRecoveryCodes() {
        return recoveryCodes;
    }

    public void setRecoveryCodes(String recoveryCodes) {
        this.recoveryCodes = recoveryCodes;
    }

    public Boolean getPrimary() {
        return isPrimary;
    }

    public void setPrimary(Boolean primary) {
        isPrimary = primary;
    }

    public Boolean getEnabled() {
        return isEnabled;
    }

    public void setEnabled(Boolean enabled) {
        isEnabled = enabled;
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

    public enum MfaMethodType {
        TOTP,
        SMS,
        EMAIL,
        RECOVERY_CODES
    }
}
