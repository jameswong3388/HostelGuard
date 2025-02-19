package org.example.hvvs.modules.common.service;

import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import org.example.hvvs.model.Notifications;
import org.example.hvvs.model.NotificationsFacade;
import org.example.hvvs.model.Users;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

@Stateless
public class NotificationServiceImpl implements NotificationService {
    @EJB
    private NotificationsFacade notificationsFacade;

    @Override
    public void createNotification(Users user, Notifications.NotificationType type, String title, String message,
                                   String relatedEntityType, String relatedEntityId) {

        Notifications notification = new Notifications();
        notification.setUser(user);
        notification.setType(type);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setRelatedEntityType(relatedEntityType);
        notification.setRelatedEntityId(relatedEntityId);
        notification.setStatus(Notifications.NotificationStatus.UNREAD);
        notificationsFacade.create(notification);
    }

    @Override
    public List<Notifications> getUnreadNotifications(Users user) {
        return notificationsFacade.findUnreadByUser(user);
    }

    @Override
    public void markAllAsRead(Users user) {
        notificationsFacade.markAllAsRead(user);
    }

    @Override
    public void markAsRead(Notifications notification) {
        notification.setStatus(Notifications.NotificationStatus.READ);
        notificationsFacade.edit(notification);
    }

    @Override
    public void deleteNotification(Notifications notification) {
        notificationsFacade.remove(notification);
    }

    @Override
    public void deleteAllNotifications(Users user) {
        notificationsFacade.findUnreadByUser(user).forEach(notificationsFacade::remove);
    }

    @Override
    public void updateNotification(Notifications notification) {
        notificationsFacade.edit(notification);
    }
}
