package org.example.hvvs.modules.admin.controller;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.example.hvvs.model.VisitorRecords;
import org.example.hvvs.modules.admin.service.VisitorRecordService;
import org.primefaces.event.RowEditEvent;

import java.io.Serializable;
import java.util.List;

@Named
@ViewScoped
public class VisitorRecordController implements Serializable {
    
    @Inject
    private VisitorRecordService visitorRecordService;
    
    private List<VisitorRecords> records;
    private List<VisitorRecords> filteredRecords;
    private List<VisitorRecords> selectedRecords;
    
    @PostConstruct
    public void init() {
        records = visitorRecordService.getAllVisitorRecords();
    }
    
    public void deleteSelectedRecords() {
        if (selectedRecords != null && !selectedRecords.isEmpty()) {
            for (VisitorRecords record : selectedRecords) {
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
    
    public void onRowEdit(RowEditEvent<VisitorRecords> event) {
        VisitorRecords record = event.getObject();
        visitorRecordService.updateVisitorRecord(record);
        FacesContext.getCurrentInstance().addMessage(null,
            new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Record updated"));
    }
    
    public void onRowCancel(RowEditEvent<VisitorRecords> event) {
        FacesContext.getCurrentInstance().addMessage(null,
            new FacesMessage(FacesMessage.SEVERITY_INFO, "Cancelled", "Edit cancelled"));
    }
    
    public boolean globalFilterFunction(Object value, Object filter, String filterField) {
        String filterText = (filter == null) ? null : filter.toString().trim().toLowerCase();
        if (filterText == null || filterText.isEmpty()) {
            return true;
        }

        VisitorRecords record = (VisitorRecords) value;
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
    public List<VisitorRecords> getRecords() {
        return records;
    }
    
    public void setRecords(List<VisitorRecords> records) {
        this.records = records;
    }
    
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
} 