package org.example.hvvs.modules.common.service;

import org.example.hvvs.model.Notifications;
import org.example.hvvs.model.Users;

import java.util.List;

public interface NotificationService {
    void createNotification(Users user, Notifications.NotificationType type, String title, String message,
                            String relatedEntityType, String relatedEntityId);

    List<Notifications> getUnreadNotifications(Users user);

    void markAllAsRead(Users user);

    void markAsRead(Notifications notification);

    void deleteNotification(Notifications notification);

    void deleteAllNotifications(Users user);

    void updateNotification(Notifications notification);
}
