package org.example.hvvs.model;

import jakarta.persistence.*;

import java.sql.Timestamp;

@Entity
@Table(name = "notifications")
@NamedQueries({
    @NamedQuery(name = "Notification.findUnreadByUser",
        query = "SELECT n FROM Notifications n WHERE n.user = :user AND n.status = 'UNREAD' ORDER BY n.createdAt DESC"),
    @NamedQuery(name = "Notification.markAllAsRead", 
        query = "UPDATE Notifications n SET n.status = 'READ', n.readAt = CURRENT_TIMESTAMP WHERE n.user = :user")
})
public class Notifications extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;
    
    @Column(nullable = false)
    private String title;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationStatus status = NotificationStatus.UNREAD;
    
    @Column(name = "related_entity_type")
    private String relatedEntityType;
    
    @Column(name = "related_entity_id")
    private String relatedEntityId;
    
    @Column(name = "read_at")
    private Timestamp readAt;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @Column(name = "updated_at")
    private Timestamp updatedAt;

    public enum NotificationType {
        VISIT_APPROVAL, SECURITY_ALERT, SYSTEM_UPDATE, VISIT_REMINDER, ENTRY_EXIT
    }

    public enum NotificationStatus {
        UNREAD, READ
    }
    
    // Getters and setters
    public Users getUser() {
        return user;
    }

    public void setUser(Users user) {
        this.user = user;
    }

    public NotificationType getType() {
        return type;
    }

    public void setType(NotificationType type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public NotificationStatus getStatus() {
        return status;
    }

    public void setStatus(NotificationStatus status) {
        this.status = status;
    }

    public String getRelatedEntityType() {
        return relatedEntityType;
    }

    public void setRelatedEntityType(String relatedEntityType) {
        this.relatedEntityType = relatedEntityType;
    }

    public String getRelatedEntityId() {
        return relatedEntityId;
    }

    public void setRelatedEntityId(String relatedEntityId) {
        this.relatedEntityId = relatedEntityId;
    }
    
    public Timestamp getReadAt() {
        return readAt;
    }

    public void setReadAt(Timestamp readAt) {
        this.readAt = readAt;
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