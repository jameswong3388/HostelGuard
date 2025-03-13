package org.example.hvvs.modules.admin.controller;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.example.hvvs.model.Notifications;
import org.example.hvvs.model.Users;
import org.example.hvvs.model.VisitRequests;
import org.example.hvvs.model.VisitRequestsFacade;
import org.example.hvvs.modules.common.service.AuditLogService;
import org.example.hvvs.modules.common.service.NotificationService;
import org.example.hvvs.utils.CommonParam;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;
import org.primefaces.model.FilterMeta;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Named("VisitRequestsControllerAdmin")
@ViewScoped
public class VisitRequestsController implements Serializable {

    @EJB
    private VisitRequestsFacade visitRequestsFacade;

    @EJB
    private NotificationService notificationService;

    @EJB
    private AuditLogService auditLogService;

    private List<VisitRequests> filteredRequests;
    private List<VisitRequests> selectedRequests;

    // New field to hold the request being edited via sidebar
    private VisitRequests editingRequest;

    private LazyDataModel<VisitRequests> lazyRequestsModel;
    private String globalFilter;
    private String exportFormat = "xlsx"; // Default format

    @PostConstruct
    public void init() {
        initializeLazyModel();

        // Log page access when controller is initialized
        try {
            Users currentUser = getCurrentUser();
            if (currentUser != null) {
                HttpServletRequest request = getHttpServletRequest();
                auditLogService.logRead(
                        currentUser,
                        "VISIT_REQUESTS_LIST",
                        null,
                        "Accessed visit requests management page",
                        request
                );
            }
        } catch (Exception e) {
            // Silent catch - don't disrupt the UI for logging errors
        }
    }

    private void initializeLazyModel() {
        lazyRequestsModel = new LazyDataModel<VisitRequests>() {
            @Override
            public int count(Map<String, FilterMeta> map) {
                return 0;
            }

            @Override
            public List<VisitRequests> load(int first, int pageSize,
                                            Map<String, SortMeta> sortBy, Map<String, FilterMeta> filterBy) {
                List<VisitRequests> results = visitRequestsFacade.findRange(
                        first,
                        pageSize,
                        globalFilter,
                        sortBy
                );
                lazyRequestsModel.setRowCount(visitRequestsFacade.count(globalFilter));
                return results;
            }

            @Override
            public VisitRequests getRowData(String rowKey) {
                return visitRequestsFacade.find(Integer.valueOf(rowKey));
            }

            @Override
            public String getRowKey(VisitRequests request) {
                return String.valueOf(request.getId());
            }
        };
    }

    public String getDeleteSelectedButtonLabel() {
        if (selectedRequests == null || selectedRequests.isEmpty()) {
            return "Delete Selected Visit Request";
        }
        return selectedRequests.size() > 1 ?
                String.format("Delete (%d requests)", selectedRequests.size()) :
                "Delete (1 request)";
    }

    public void deleteSelectedRequests() {
        if (selectedRequests != null && !selectedRequests.isEmpty()) {
            try {
                // Get current user from session for audit logging
                Users currentUser = getCurrentUser();
                HttpServletRequest request = getHttpServletRequest();

                for (VisitRequests req : selectedRequests) {
                    VisitRequests managedRequest = visitRequestsFacade.find(req.getId());
                    if (managedRequest != null) {
                        // Prepare audit information before deletion
                        String oldValues = String.format(
                                "{\"id\":%d,\"verificationCode\":\"%s\",\"unitNumber\":\"%s\",\"purpose\":\"%s\",\"status\":\"%s\"}",
                                managedRequest.getId(),
                                managedRequest.getVerificationCode(),
                                managedRequest.getUnitNumber(),
                                managedRequest.getPurpose(),
                                managedRequest.getStatus()
                        );

                        // Log the deletion
                        auditLogService.logDelete(
                                currentUser,
                                "VISIT_REQUESTS",
                                managedRequest.getId().toString(),
                                "Deleted visit request for unit: " + managedRequest.getUnitNumber(),
                                oldValues,
                                request
                        );

                        visitRequestsFacade.remove(managedRequest);
                    }
                }
                selectedRequests.clear();
                lazyRequestsModel.setRowCount(visitRequestsFacade.count(globalFilter));

                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Selected requests deleted successfully"));
            } catch (Exception e) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Failed to delete selected requests"));
            }
        }
    }

    public void clearSelection() {
        if (selectedRequests != null) {
            selectedRequests.clear();
        }
    }

    public boolean globalFilterFunction(Object value, Object filter, String filterLocale) {
        String filterText = (filter == null) ? null : filter.toString().trim().toLowerCase();
        if (filterText == null || filterText.isEmpty()) {
            return true;
        }

        VisitRequests request = (VisitRequests) value;
        return request.getVerificationCode().toLowerCase().contains(filterText)
                || request.getUnitNumber().toLowerCase().contains(filterText)
                || request.getPurpose().toLowerCase().contains(filterText)
                || request.getStatus().toString().contains(filterText)
                || request.getRemarks().toLowerCase().contains(filterText);
    }

    // --- Methods for Sidebar Editing ---

    /**
     * Called when an admin clicks the "Edit" button.
     * Prepares the selected visit request for editing in the sidebar.
     *
     * @param request the VisitRequests record to edit
     */
    public void prepareEdit(VisitRequests request) {
        this.editingRequest = request;

        // Log request view for audit
        try {
            Users currentUser = getCurrentUser();
            if (currentUser != null) {
                HttpServletRequest httpRequest = getHttpServletRequest();
                auditLogService.logRead(
                        currentUser,
                        "VISIT_REQUESTS",
                        request.getId().toString(),
                        "Viewed details for visit request to unit: " + request.getUnitNumber(),
                        httpRequest
                );
            }
        } catch (Exception e) {
            // Silent catch - don't disrupt the UI for logging errors
        }
    }

    /**
     * Updates the edited visit request.
     */
    @Transactional
    public void updateRequest() {
        try {
            // Get current user from session for audit logging
            Users currentUser = getCurrentUser();
            HttpServletRequest request = getHttpServletRequest();

            // Get original request for comparison
            VisitRequests original = visitRequestsFacade.find(editingRequest.getId());
            VisitRequests.VisitStatus originalStatus = original.getStatus(); // Capture status before changes

            // Prepare old values for audit logging
            String oldValues = String.format(
                    "{\"verificationCode\":\"%s\",\"unitNumber\":\"%s\",\"purpose\":\"%s\",\"status\":\"%s\",\"remarks\":\"%s\"}",
                    original.getVerificationCode(),
                    original.getUnitNumber(),
                    original.getPurpose(),
                    original.getStatus(),
                    original.getRemarks()
            );

            visitRequestsFacade.edit(editingRequest);

            // Prepare new values for audit logging
            String newValues = String.format(
                    "{\"verificationCode\":\"%s\",\"unitNumber\":\"%s\",\"purpose\":\"%s\",\"status\":\"%s\",\"remarks\":\"%s\"}",
                    editingRequest.getVerificationCode(),
                    editingRequest.getUnitNumber(),
                    editingRequest.getPurpose(),
                    editingRequest.getStatus(),
                    editingRequest.getRemarks()
            );

            // Log the update
            auditLogService.logUpdate(
                    currentUser,
                    "VISIT_REQUESTS",
                    editingRequest.getId().toString(),
                    "Updated visit request for unit: " + editingRequest.getUnitNumber(),
                    oldValues,
                    newValues,
                    request
            );

            if (!originalStatus.equals(editingRequest.getStatus())) {
                notificationService.createNotification(
                        editingRequest.getUserId(),
                        Notifications.NotificationType.SYSTEM_UPDATE,
                        "Visit Request Status Updated",
                        "Your visit request #" + editingRequest.getId() + " status has changed to " + editingRequest.getStatus(),
                        "visit-requests",
                        String.valueOf(editingRequest.getId())
                );
            }

            // Refresh the table
            lazyRequestsModel.setRowCount(visitRequestsFacade.count(globalFilter));

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Visit request updated successfully"));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Failed to update visit request: " + e.getMessage()));
            FacesContext.getCurrentInstance().validationFailed();
        }
    }

    /**
     * Get the current user from the session
     */
    private Users getCurrentUser() {
        return (Users) FacesContext.getCurrentInstance()
                .getExternalContext().getSessionMap().get(CommonParam.SESSION_SELF);
    }

    /**
     * Get the current HttpServletRequest
     */
    private HttpServletRequest getHttpServletRequest() {
        return (HttpServletRequest) FacesContext.getCurrentInstance()
                .getExternalContext().getRequest();
    }

    // --- Getters and Setters ---
    public List<VisitRequests> getFilteredRequests() {
        return filteredRequests;
    }

    public void setFilteredRequests(List<VisitRequests> filteredRequests) {
        this.filteredRequests = filteredRequests;
    }

    public List<VisitRequests> getSelectedRequests() {
        return selectedRequests;
    }

    public void setSelectedRequests(List<VisitRequests> selectedRequests) {
        this.selectedRequests = selectedRequests;
    }

    public VisitRequests getEditingRequest() {
        return editingRequest;
    }

    public void setEditingRequest(VisitRequests editingRequest) {
        this.editingRequest = editingRequest;
    }

    public LazyDataModel<VisitRequests> getLazyRequestsModel() {
        return lazyRequestsModel;
    }

    public void setLazyRequestsModel(LazyDataModel<VisitRequests> lazyRequestsModel) {
        this.lazyRequestsModel = lazyRequestsModel;
    }

    public String getGlobalFilter() {
        return globalFilter;
    }

    public void setGlobalFilter(String globalFilter) {
        this.globalFilter = globalFilter;
    }

    public String getExportFormat() {
        return exportFormat;
    }

    public void setExportFormat(String exportFormat) {
        this.exportFormat = exportFormat;
    }
}
