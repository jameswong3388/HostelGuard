package org.example.hvvs.modules.admin.controller;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import java.io.Serial;
import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.example.hvvs.model.UsersFacade;
import org.example.hvvs.model.Users.Role;
import org.example.hvvs.model.VisitRequestsFacade;
import org.example.hvvs.model.NotificationsFacade;
import org.example.hvvs.model.VisitorRecordsFacade;
import org.primefaces.event.CloseEvent;
import org.primefaces.event.DashboardReorderEvent;
import org.primefaces.event.ToggleEvent;
import org.primefaces.model.dashboard.DashboardModel;
import org.primefaces.model.dashboard.DashboardWidget;
import org.primefaces.model.dashboard.DefaultDashboardModel;
import org.primefaces.model.dashboard.DefaultDashboardWidget;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.example.hvvs.model.VisitRequests;
import org.example.hvvs.model.Notifications;

@Named
@ViewScoped
public class DashboardView implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    private static final String RESPONSIVE_CLASS = "col-12 lg:col-6 xl:col-6";

    @EJB
    private UsersFacade usersFacade;

    @EJB
    private VisitRequestsFacade visitRequestsFacade;

    @EJB
    private NotificationsFacade notificationsFacade;

    @EJB
    private VisitorRecordsFacade visitorRecordsFacade;

    @PersistenceContext(unitName = "HostelGuard")
    private EntityManager em;

    private DashboardModel responsiveModel;
    
    // Statistics counters
    private int totalUsers;
    private int totalResidents;
    private int totalSecurity;
    private int totalManagingStaff;
    private int pendingVisitRequests;
    private int currentVisitors;
    private int unreadNotifications;
    private int todaysEvents;

    private Map<String, String> resizeWidths = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        // responsive
        responsiveModel = new DefaultDashboardModel();
        responsiveModel.addWidget(new DefaultDashboardWidget("bar", RESPONSIVE_CLASS));

        // legacy
        loadStatistics();
    }
    
    private void loadStatistics() {
        // Get total number of users
        totalUsers = usersFacade.count();
        
        // Get total number of residents
        totalResidents = usersFacade.countByRole(Role.RESIDENT);
        
        // Get total number of security staff
        totalSecurity = usersFacade.countByRole(Role.SECURITY_STAFF);
        
        // Get total number of managing staff
        totalManagingStaff = usersFacade.countByRole(Role.MANAGING_STAFF);
        
        // Get total number of pending visit requests
        pendingVisitRequests = countPendingVisitRequests();
        
        // Get total number of current visitors
        currentVisitors = countCurrentVisitors();
        
        // Get total number of unread notifications
        unreadNotifications = countUnreadNotifications();
        
        // Get total number of todays events
        todaysEvents = 0;
    }

    /**
     * Count the number of visit requests with PENDING status
     */
    private int countPendingVisitRequests() {
        try {
            return em.createQuery(
                    "SELECT COUNT(v) FROM VisitRequests v WHERE v.status = :status",
                    Long.class)
                    .setParameter("status", VisitRequests.VisitStatus.PENDING)
                    .getSingleResult()
                    .intValue();
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Count the number of visitor records that are currently checked in but not checked out
     */
    private int countCurrentVisitors() {
        try {
            return em.createQuery(
                    "SELECT COUNT(v) FROM VisitorRecords v WHERE v.check_in_time IS NOT NULL AND v.check_out_time IS NULL",
                    Long.class)
                    .getSingleResult()
                    .intValue();
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Count the number of unread notifications
     */
    private int countUnreadNotifications() {
        try {
            return em.createQuery(
                    "SELECT COUNT(n) FROM Notifications n WHERE n.status = :status",
                    Long.class)
                    .setParameter("status", Notifications.NotificationStatus.UNREAD)
                    .getSingleResult()
                    .intValue();
        } catch (Exception e) {
            return 0;
        }
    }

    public void handleReorder(DashboardReorderEvent event) {
        FacesMessage message = new FacesMessage();
        message.setSeverity(FacesMessage.SEVERITY_INFO);
        message.setSummary("Reordered: " + event.getWidgetId());
        String result = String.format("Dragged index: %d, Dropped Index: %d, Widget Index: %d",
                event.getSenderColumnIndex(), event.getColumnIndex(), event.getItemIndex());
        message.setDetail(result);

        addMessage(message);
    }

    public void handleClose(CloseEvent event) {
        FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_WARN, "Panel Closed",
                "Closed panel ID:'" + event.getComponent().getId() + "'");

        addMessage(message);
    }

    public void handleToggle(ToggleEvent event) {
        FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Panel Toggled",
                "Toggle panel ID:'" + event.getComponent().getId() + "' Status:" + event.getVisibility().name());

        addMessage(message);
    }

    /**
     * Dashboard panel has been resized.
     *
     * @param widget the DashboardPanel
     * @param size   the new size CSS
     */
    public void onDashboardResize(final String widget, final String size) {
        final DashboardWidget dashboard = responsiveModel.getWidget(widget);
        if (dashboard != null) {
            final String newCss = dashboard.getStyleClass().replaceFirst("xl:col-\\d+", size);
            dashboard.setStyleClass(newCss);
        }
        resizeWidths.put(widget, size);
    }

    private void addMessage(FacesMessage message) {
        FacesContext.getCurrentInstance().addMessage(null, message);
    }

    public DashboardModel getResponsiveModel() {
        return responsiveModel;
    }
    
    // Getters for statistics
    public int getTotalUsers() {
        return totalUsers;
    }
    
    public int getTotalResidents() {
        return totalResidents;
    }
    
    public int getTotalSecurity() {
        return totalSecurity;
    }
    
    public int getTotalManagingStaff() {
        return totalManagingStaff;
    }
    
    public int getPendingVisitRequests() {
        return pendingVisitRequests;
    }
    
    public int getCurrentVisitors() {
        return currentVisitors;
    }
    
    public int getUnreadNotifications() {
        return unreadNotifications;
    }
    
    public int getTodaysEvents() {
        return todaysEvents;
    }

    public Map<String, String> getResizeWidths() {
        return resizeWidths;
    }
}