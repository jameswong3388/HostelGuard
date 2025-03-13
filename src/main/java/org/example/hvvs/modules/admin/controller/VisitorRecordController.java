package org.example.hvvs.modules.admin.controller;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.example.hvvs.model.Users;
import org.example.hvvs.model.VisitorRecords;
import org.example.hvvs.model.VisitorRecordsFacade;
import org.example.hvvs.modules.common.service.AuditLogService;
import org.example.hvvs.utils.CommonParam;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;
import org.primefaces.model.FilterMeta;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Named
@ViewScoped
public class VisitorRecordController implements Serializable {
    @EJB
    private VisitorRecordsFacade visitorRecordsFacade;

    @EJB
    private AuditLogService auditLogService;

    private List<VisitorRecords> filteredRecords;
    private List<VisitorRecords> selectedRecords;

    // New field to hold the record being edited via sidebar
    private VisitorRecords editingRecord;

    private LazyDataModel<VisitorRecords> lazyRecordsModel;
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
                        "VISITOR_RECORDS_LIST",
                        null,
                        "Accessed visitor records management page",
                        request
                );
            }
        } catch (Exception e) {
            // Silent catch - don't disrupt the UI for logging errors
        }
    }

    private void initializeLazyModel() {
        lazyRecordsModel = new LazyDataModel<VisitorRecords>() {
            @Override
            public int count(Map<String, FilterMeta> map) {
                return 0;
            }

            @Override
            public List<VisitorRecords> load(int first, int pageSize,
                                             Map<String, SortMeta> sortBy, Map<String, FilterMeta> filterBy) {
                List<VisitorRecords> results = visitorRecordsFacade.findRange(
                        first,
                        pageSize,
                        globalFilter,
                        sortBy
                );
                lazyRecordsModel.setRowCount(visitorRecordsFacade.count(globalFilter));
                return results;
            }

            @Override
            public VisitorRecords getRowData(String rowKey) {
                return visitorRecordsFacade.find(Integer.valueOf(rowKey));
            }

            @Override
            public String getRowKey(VisitorRecords record) {
                return String.valueOf(record.getId());
            }
        };
    }

    public void deleteSelectedRecords() {
        if (selectedRecords != null && !selectedRecords.isEmpty()) {
            try {
                // Get current user from session for audit logging
                Users currentUser = getCurrentUser();
                HttpServletRequest request = getHttpServletRequest();

                for (VisitorRecords selectedRecord : selectedRecords) {
                    VisitorRecords managedRecord = visitorRecordsFacade.find(selectedRecord.getId());
                    if (managedRecord != null) {
                        // Prepare audit information before deletion
                        String oldValues = String.format(
                                "{\"id\":%d,\"visitorName\":\"%s\",\"visitorIc\":\"%s\",\"visitorPhone\":\"%s\"}",
                                managedRecord.getId(),
                                managedRecord.getVisitorName(),
                                managedRecord.getVisitorIc(),
                                managedRecord.getVisitorPhone()
                        );

                        // Log the deletion
                        auditLogService.logDelete(
                                currentUser,
                                "VISITOR_RECORDS",
                                managedRecord.getId().toString(),
                                "Deleted visitor record for: " + managedRecord.getVisitorName(),
                                oldValues,
                                request
                        );

                        visitorRecordsFacade.remove(managedRecord);
                    }
                }
                selectedRecords.clear();
                init(); // Refresh the list
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Selected records deleted successfully"));
            } catch (Exception e) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Failed to delete selected records"));
            }
        }
    }

    public void clearSelection() {
        if (selectedRecords != null) {
            selectedRecords.clear();
        }
    }

    // --- Methods for Sidebar Editing ---

    /**
     * Called when an admin clicks the "Edit" button.
     * Prepares the selected visitor record for editing in the sidebar.
     *
     * @param record the VisitorRecords record to edit
     */
    public void prepareEdit(VisitorRecords record) {
        this.editingRecord = record;

        // Log record view for audit
        try {
            Users currentUser = getCurrentUser();
            if (currentUser != null) {
                HttpServletRequest request = getHttpServletRequest();
                auditLogService.logRead(
                        currentUser,
                        "VISITOR_RECORDS",
                        record.getId().toString(),
                        "Viewed details for visitor: " + record.getVisitorName(),
                        request
                );
            }
        } catch (Exception e) {
            // Silent catch - don't disrupt the UI for logging errors
        }
    }

    /**
     * Updates the edited visitor record.
     */
    @Transactional
    public void updateRecord() {
        try {
            // Get current user from session for audit logging
            Users currentUser = getCurrentUser();
            HttpServletRequest request = getHttpServletRequest();

            // Get original record for comparison
            VisitorRecords originalRecord = visitorRecordsFacade.find(editingRecord.getId());

            // Prepare old values for audit logging
            String oldValues = String.format(
                    "{\"visitorName\":\"%s\",\"visitorIc\":\"%s\",\"visitorPhone\":\"%s\",\"checkInTime\":\"%s\",\"checkOutTime\":\"%s\"}",
                    originalRecord.getVisitorName(),
                    originalRecord.getVisitorIc(),
                    originalRecord.getVisitorPhone(),
                    originalRecord.getCheckInTime(),
                    originalRecord.getCheckOutTime()
            );

            // Update the record
            visitorRecordsFacade.edit(editingRecord);

            // Prepare new values for audit logging
            String newValues = String.format(
                    "{\"visitorName\":\"%s\",\"visitorIc\":\"%s\",\"visitorPhone\":\"%s\",\"checkInTime\":\"%s\",\"checkOutTime\":\"%s\"}",
                    editingRecord.getVisitorName(),
                    editingRecord.getVisitorIc(),
                    editingRecord.getVisitorPhone(),
                    editingRecord.getCheckInTime(),
                    editingRecord.getCheckOutTime()
            );

            // Log the update
            auditLogService.logUpdate(
                    currentUser,
                    "VISITOR_RECORDS",
                    editingRecord.getId().toString(),
                    "Updated visitor record for: " + editingRecord.getVisitorName(),
                    oldValues,
                    newValues,
                    request
            );

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Visitor record updated successfully"));
            init(); // Refresh the list after update
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Failed to update visitor record: " + e.getMessage()));
            FacesContext.getCurrentInstance().validationFailed();
        }
    }

    public boolean globalFilterFunction(Object value, Object filter, String filterLocale) {
        String filterText = (filter == null) ? null : filter.toString().trim().toLowerCase();
        if (filterText == null || filterText.isEmpty()) {
            return true;
        }

        VisitorRecords record = (VisitorRecords) value;
        return record.getVisitorName().toLowerCase().contains(filterText)
                || record.getVisitorIc().toLowerCase().contains(filterText)
                || record.getVisitorPhone().toLowerCase().contains(filterText)
                || record.getSecurityStaffId().getUsername().toLowerCase().contains(filterText)
                || record.getRequestId().getUnitNumber().toLowerCase().contains(filterText);
    }

    public String getDeleteSelectedButtonLabel() {
        if (selectedRecords == null || selectedRecords.isEmpty()) {
            return "Delete Selected Visitor Record";
        }
        return selectedRecords.size() > 1 ?
                String.format("Delete (%d records)", selectedRecords.size()) :
                "Delete (1 record)";
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

    // Getters and Setters
    public List<VisitorRecords> getFilteredRecords() {
        return filteredRecords;
    }

    public void setFilteredRecords(List<VisitorRecords> filteredRecords) {
        this.filteredRecords = filteredRecords;
    }

    public List<VisitorRecords> getSelectedRecords() {
        return selectedRecords;
    }

    public void setSelectedRecords(List<VisitorRecords> selectedRecords) {
        this.selectedRecords = selectedRecords;
    }

    public VisitorRecords getEditingRecord() {
        return editingRecord;
    }

    public void setEditingRecord(VisitorRecords editingRecord) {
        this.editingRecord = editingRecord;
    }

    public LazyDataModel<VisitorRecords> getLazyRecordsModel() {
        return lazyRecordsModel;
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