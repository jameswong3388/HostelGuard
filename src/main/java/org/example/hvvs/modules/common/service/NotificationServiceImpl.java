package org.example.hvvs.modules.common.service;

import jakarta.ejb.EJB;
import org.example.hvvs.model.Notifications;
import org.example.hvvs.model.NotificationsFacade;
import org.example.hvvs.model.Users;
import org.example.hvvs.modules.common.service.NotificationService;

import java.util.List;

public class NotificationServiceImpl implements NotificationService {
    @EJB
    private NotificationsFacade notificationsFacade;

    public void createNotification(Users user, Notifications.NotificationType type, String title, String message,
                                   String relatedEntityType, String relatedEntityId) {

        Notifications notification = new Notifications();
        notification.setUser(user);
        notification.setType(type);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setRelatedEntityType(relatedEntityType);
        notification.setRelatedEntityId(relatedEntityId);
        notificationsFacade.create(notification);
    }

    public List<Notifications> getUnreadNotifications(Users user) {
        return notificationsFacade.findUnreadByUser(user);
    }

    public void markAllAsRead(Users user) {
        notificationsFacade.markAllAsRead(user);
    }

    public void markAsRead(Notifications notification) {
        notification.setStatus(Notifications.NotificationStatus.READ);
        notificationsFacade.edit(notification);
    }

    public void deleteNotification(Notifications notification) {
        notificationsFacade.remove(notification);
    }

    public void deleteAllNotifications(Users user) {
        List<Notifications> notifications = notificationsFacade.findAll();
        for (Notifications notification : notifications) {
            if (notification.getUser().equals(user)) {
                notificationsFacade.remove(notification);
            }
        }
    }

    public void updateNotification(Notifications notification) {
        notificationsFacade.edit(notification);
    }
}
