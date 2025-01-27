package org.example.hvvs.modules.admin.controller;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.example.hvvs.model.VisitorRecord;
import org.example.hvvs.modules.admin.service.VisitorRecordService;
import org.primefaces.event.RowEditEvent;

import java.io.Serializable;
import java.util.List;

@Named
@ViewScoped
public class VisitorRecordController implements Serializable {
    
    @Inject
    private VisitorRecordService visitorRecordService;
    
    private List<VisitorRecord> records;
    private List<VisitorRecord> filteredRecords;
    private List<VisitorRecord> selectedRecords;
    
    @PostConstruct
    public void init() {
        records = visitorRecordService.getAllVisitorRecords();
    }
    
    public void deleteSelectedRecords() {
        if (selectedRecords != null && !selectedRecords.isEmpty()) {
            for (VisitorRecord record : selectedRecords) {
                visitorRecordService.deleteVisitorRecord(record);
            }
            records = visitorRecordService.getAllVisitorRecords();
            selectedRecords = null;
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Selected records deleted"));
        }
    }
    
    public void clearSelection() {
        selectedRecords = null;
    }
    
    public void onRowEdit(RowEditEvent<VisitorRecord> event) {
        VisitorRecord record = event.getObject();
        visitorRecordService.updateVisitorRecord(record);
        FacesContext.getCurrentInstance().addMessage(null,
            new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Record updated"));
    }
    
    public void onRowCancel(RowEditEvent<VisitorRecord> event) {
        FacesContext.getCurrentInstance().addMessage(null,
            new FacesMessage(FacesMessage.SEVERITY_INFO, "Cancelled", "Edit cancelled"));
    }
    
    public boolean globalFilterFunction(Object value, Object filter, String filterField) {
        String filterText = (filter == null) ? null : filter.toString().trim().toLowerCase();
        if (filterText == null || filterText.isEmpty()) {
            return true;
        }

        VisitorRecord record = (VisitorRecord) value;
        return record.getVisitorName().toLowerCase().contains(filterText)
            || record.getVisitorIc().toLowerCase().contains(filterText)
            || record.getVisitorPhone().toLowerCase().contains(filterText)
            || (record.getRemarks() != null && record.getRemarks().toLowerCase().contains(filterText));
    }
    
    public String getDeleteSelectedButtonLabel() {
        if (selectedRecords == null || selectedRecords.isEmpty()) {
            return "Delete";
        }
        return String.format("Delete (%d)", selectedRecords.size());
    }
    
    // Getters and Setters
    public List<VisitorRecord> getRecords() {
        return records;
    }
    
    public void setRecords(List<VisitorRecord> records) {
        this.records = records;
    }
    
    public List<VisitorRecord> getFilteredRecords() {
        return filteredRecords;
    }
    
    public void setFilteredRecords(List<VisitorRecord> filteredRecords) {
        this.filteredRecords = filteredRecords;
    }
    
    public List<VisitorRecord> getSelectedRecords() {
        return selectedRecords;
    }
    
    public void setSelectedRecords(List<VisitorRecord> selectedRecords) {
        this.selectedRecords = selectedRecords;
    }
} 