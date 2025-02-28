package org.example.hvvs.model;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Table(name = "password_reset_tokens")
@NamedQueries({
        @NamedQuery(name = "PasswordResetToken.findByToken", 
                  query = "SELECT p FROM PasswordResetTokens p WHERE p.token = :token"),
        @NamedQuery(name = "PasswordResetToken.findByUser", 
                  query = "SELECT p FROM PasswordResetTokens p WHERE p.user = :user"),
        @NamedQuery(name = "PasswordResetToken.findValidToken", 
                  query = "SELECT p FROM PasswordResetTokens p WHERE p.token = :token AND p.used = false AND p.expiresAt > CURRENT_TIMESTAMP")
})
public class PasswordResetTokens {
    @Id
    @UuidGenerator
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(
            name = "id",
            columnDefinition = "CHAR(36)"
    )
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    @Column(name = "token", nullable = false, unique = true)
    private String token;

    @Column(name = "used")
    private Boolean used = false;

    @Column(name = "expires_at", nullable = false)
    private Timestamp expiresAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Timestamp createdAt;

    @Column(name = "updated_at", nullable = false)
    private Timestamp updatedAt;

    /**
     * Default constructor
     */
    public PasswordResetTokens() {
        super();
    }

    /**
     * Constructor with parameters
     * 
     * @param user The user requesting the password reset
     * @param token The unique token for password reset
     * @param expiresAt The token expiration timestamp
     */
    public PasswordResetTokens(Users user, String token, Timestamp expiresAt) {
        this.user = user;
        this.token = token;
        this.expiresAt = expiresAt;
        this.used = false;
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

    // Getters and Setters

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Users getUser() {
        return user;
    }

    public void setUser(Users user) {
        this.user = user;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Boolean getUsed() {
        return used;
    }

    public void setUsed(Boolean used) {
        this.used = used;
    }

    public Timestamp getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Timestamp expiresAt) {
        this.expiresAt = expiresAt;
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

    /**
     * Checks if the token is still valid
     * @return true if the token is not used and not expired
     */
    public boolean isValid() {
        Timestamp now = new Timestamp(System.currentTimeMillis());
        return !used && expiresAt.after(now);
    }
} 