package org.example.hvvs.modules.god.controller;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import org.example.hvvs.model.AuditLogs;
import org.example.hvvs.model.AuditLogsFacade;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;
import org.primefaces.model.FilterMeta;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Named
@ViewScoped
public class AuditLogsController implements Serializable {
    @EJB
    private AuditLogsFacade auditLogsFacade;

    private LazyDataModel<AuditLogs> lazyAuditLogsModel;
    private String globalFilter;
    private List<AuditLogs> filteredAuditLogs;
    private List<AuditLogs> selectedAuditLogs;
    private AuditLogs selectedAuditLog;
    private String exportFormat = "xlsx"; // Default format

    @PostConstruct
    public void init() {
        initializeLazyModel();
    }

    private void initializeLazyModel() {
        lazyAuditLogsModel = new LazyDataModel<AuditLogs>() {
            @Override
            public int count(Map<String, FilterMeta> map) {
                return 0;
            }

            @Override
            public List<AuditLogs> load(int first, int pageSize, Map<String, SortMeta> sortBy, Map<String, FilterMeta> filterBy) {
                List<AuditLogs> results = auditLogsFacade.findRange(
                    first, 
                    pageSize, 
                    globalFilter,
                    sortBy
                );
                lazyAuditLogsModel.setRowCount(auditLogsFacade.count(globalFilter));
                return results;
            }
            
            @Override
            public AuditLogs getRowData(String rowKey) {
                return auditLogsFacade.find(Integer.valueOf(rowKey));
            }
            
            @Override
            public String getRowKey(AuditLogs auditLog) {
                return String.valueOf(auditLog.getId());
            }
        };
    }

    public boolean globalFilterFunction(Object value, Object filter, String filterLocale) {
        String filterText = (filter == null) ? null : filter.toString().trim().toLowerCase();
        if (filterText == null || filterText.isEmpty()) {
            return true;
        }

        AuditLogs auditLog = (AuditLogs) value;
        return (auditLog.getUserId() != null && auditLog.getUserId().getUsername().toLowerCase().contains(filterText))
                || auditLog.getAction().toString().toLowerCase().contains(filterText)
                || auditLog.getEntityType().toLowerCase().contains(filterText)
                || (auditLog.getEntityId() != null && auditLog.getEntityId().toLowerCase().contains(filterText))
                || (auditLog.getDescription() != null && auditLog.getDescription().toLowerCase().contains(filterText))
                || (auditLog.getIpAddress() != null && auditLog.getIpAddress().toLowerCase().contains(filterText));
    }

    public void clearSelection() {
        if (selectedAuditLogs != null) {
            selectedAuditLogs.clear();
        }
    }

    private void addMessage(String summary, String detail) {
        FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, summary, detail);
        FacesContext.getCurrentInstance().addMessage(null, message);
    }

    private void addErrorMessage(String detail) {
        FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", detail);
        FacesContext.getCurrentInstance().addMessage(null, message);
    }

    public void prepareViewDetails(AuditLogs log) {
        this.selectedAuditLog = log;
    }

    // Getters and Setters
    public LazyDataModel<AuditLogs> getLazyAuditLogsModel() {
        return lazyAuditLogsModel;
    }

    public List<AuditLogs> getFilteredAuditLogs() {
        return filteredAuditLogs;
    }

    public void setFilteredAuditLogs(List<AuditLogs> filteredAuditLogs) {
        this.filteredAuditLogs = filteredAuditLogs;
    }

    public String getGlobalFilter() {
        return globalFilter;
    }
    
    public void setGlobalFilter(String globalFilter) {
        this.globalFilter = globalFilter;
    }

    public List<AuditLogs> getSelectedAuditLogs() {
        return selectedAuditLogs;
    }

    public void setSelectedAuditLogs(List<AuditLogs> selectedAuditLogs) {
        this.selectedAuditLogs = selectedAuditLogs;
    }

    public String getExportFormat() {
        return exportFormat;
    }

    public void setExportFormat(String exportFormat) {
        this.exportFormat = exportFormat;
    }
    
    public AuditLogs getSelectedAuditLog() {
        return selectedAuditLog;
    }
    
    public void setSelectedAuditLog(AuditLogs selectedAuditLog) {
        this.selectedAuditLog = selectedAuditLog;
    }
}
