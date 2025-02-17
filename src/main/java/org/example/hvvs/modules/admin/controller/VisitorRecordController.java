package org.example.hvvs.modules.admin.controller;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import jakarta.transaction.Transactional;
import org.example.hvvs.model.VisitorRecords;
import org.example.hvvs.model.VisitorRecordsFacade;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;
import org.primefaces.model.FilterMeta;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

@Named
@ViewScoped
public class VisitorRecordController implements Serializable {
    @EJB
    private VisitorRecordsFacade visitorRecordsFacade;

    private List<VisitorRecords> filteredRecords;
    private List<VisitorRecords> selectedRecords;
    
    // New field to hold the record being edited via sidebar
    private VisitorRecords editingRecord;

    private LazyDataModel<VisitorRecords> lazyRecordsModel;
    private String globalFilter;

    @PostConstruct
    public void init() {
        initializeLazyModel();
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
                for (VisitorRecords selectedRecord : selectedRecords) {
                    VisitorRecords managedRecord = visitorRecordsFacade.find(selectedRecord.getId());
                    if (managedRecord != null) {
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
    }

    /**
     * Updates the edited visitor record.
     */
    @Transactional
    public void updateRecord() {
        try {
            editingRecord.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
            visitorRecordsFacade.edit(editingRecord);
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
} 