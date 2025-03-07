package org.example.hvvs.modules.common.controllers;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import org.example.hvvs.model.Medias;
import org.example.hvvs.model.Notifications;
import org.example.hvvs.model.NotificationsFacade;
import org.example.hvvs.model.Users;
import org.example.hvvs.modules.common.service.MediaService;
import org.example.hvvs.modules.common.service.NotificationService;
import org.example.hvvs.utils.CommonParam;
import org.primefaces.model.FilterMeta;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;

import java.io.Serializable;
import java.util.List;
import java.util.Map;


@Named("notificationsController")
@ViewScoped
public class NotificationsController implements Serializable {

    @EJB
    private NotificationService notificationService;

    @EJB
    private NotificationsFacade notificationsFacade;
    
    @EJB
    private MediaService mediaService;

    private List<Notifications> filteredNotifications;
    private List<Notifications> selectedNotifications;
    private Users currentUser;

    private LazyDataModel<Notifications> lazyNotificationsModel;
    private String globalFilter;

    // Single notification actions
    private Notifications selectedNotification;
    
    // Media related fields
    private List<Medias> notificationMedia;

    @PostConstruct
    public void init() {
        currentUser = (Users) FacesContext.getCurrentInstance()
                .getExternalContext().getSessionMap().get(CommonParam.SESSION_SELF);
        initializeLazyModel();
    }

    private void initializeLazyModel() {
        lazyNotificationsModel = new LazyDataModel<Notifications>() {
            @Override
            public int count(Map<String, FilterMeta> map) {
                return 0;
            }

            @Override
            public List<Notifications> load(int first, int pageSize,
                                            Map<String, SortMeta> sortBy,
                                            Map<String, FilterMeta> filterBy) {
                List<Notifications> results = notificationsFacade.findRange(
                        first,
                        pageSize,
                        globalFilter,
                        sortBy,
                        currentUser
                );
                lazyNotificationsModel.setRowCount(notificationsFacade.count(globalFilter, currentUser));
                return results;
            }

            @Override
            public Notifications getRowData(String rowKey) {
                return notificationsFacade.find(Integer.valueOf(rowKey));
            }

            @Override
            public String getRowKey(Notifications notification) {
                return String.valueOf(notification.getId());
            }
        };
    }

    public boolean globalFilterFunction(Object value, Object filter, String filterLocale) {
        String filterText = (filter == null) ? null : filter.toString().trim().toLowerCase();
        if (filterText == null || filterText.isEmpty()) {
            return true;
        }
        Notifications notification = (Notifications) value;
        return notification.getTitle().toLowerCase().contains(filterText) ||
                notification.getMessage().toLowerCase().contains(filterText) ||
                notification.getType().toString().toLowerCase().contains(filterText);
    }

    // Filter actions
    public void clearSelection() {
        if (selectedNotifications != null) {
            selectedNotifications.clear();
        }
    }


    // Bulk actions
    public void markAllRead() {
        notificationService.markAllAsRead(currentUser);
        lazyNotificationsModel.setRowCount(notificationsFacade.count(globalFilter));
    }

    public void markSelectedAsRead() {
        selectedNotifications.forEach(notificationService::markAsRead);
        selectedNotifications.clear();
        lazyNotificationsModel.setRowCount(notificationsFacade.count(globalFilter));
    }

    public void deleteSelected() {
        selectedNotifications.forEach(notificationService::deleteNotification);
        selectedNotifications.clear();
        lazyNotificationsModel.setRowCount(notificationsFacade.count(globalFilter));
    }

    // Single notification actions
    public void markAsRead(Notifications notification) {
        notificationService.markAsRead(notification);
        // Refresh the row count if needed
        lazyNotificationsModel.setRowCount(notificationsFacade.count(globalFilter, currentUser));
    }

    public void deleteNotification(Notifications notification) {
        notificationService.deleteNotification(notification);
        // Refresh the row count if needed
        lazyNotificationsModel.setRowCount(notificationsFacade.count(globalFilter, currentUser));
    }

    // Getters
    public List<Notifications> getFilteredNotifications() {
        return filteredNotifications;
    }

    public void setFilteredNotifications(List<Notifications> filteredNotifications) {
        this.filteredNotifications = filteredNotifications;
    }

    public List<Notifications> getSelectedNotifications() {
        return selectedNotifications;
    }

    public void setSelectedNotifications(List<Notifications> selectedNotifications) {
        this.selectedNotifications = selectedNotifications;
    }

    public LazyDataModel<Notifications> getLazyNotificationsModel() {
        return lazyNotificationsModel;
    }

    public String getGlobalFilter() {
        return globalFilter;
    }

    public void setGlobalFilter(String globalFilter) {
        this.globalFilter = globalFilter;
    }

    public Notifications getSelectedNotification() {
        return selectedNotification;
    }

    public void setSelectedNotification(Notifications selectedNotification) {
        this.selectedNotification = selectedNotification;
    }

    /**
     * Prepares a notification for viewing in the sidebar
     * 
     * @param notification The notification to view
     */
    public void prepareView(Notifications notification) {
        this.selectedNotification = notification;
        List<Medias> medias = mediaService.findByModelAndModelId("notifications", selectedNotification.getId().toString());
        setNotificationMedia(medias);
    }

    public List<Medias> getNotificationMedia() {
        return notificationMedia;
    }

    public void setNotificationMedia(List<Medias> notificationMedia) {
        this.notificationMedia = notificationMedia;
    }
}
