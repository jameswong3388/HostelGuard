package org.example.hvvs.modules.admin.controller;

import jakarta.ejb.EJB;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import jakarta.transaction.Transactional;
import org.example.hvvs.model.VisitRequests;
import org.example.hvvs.model.VisitRequestsFacade;
import org.primefaces.event.RowEditEvent;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;
import java.util.Locale;

@Named
@ViewScoped
public class VisitRequestsController implements Serializable {

    @EJB
    private VisitRequestsFacade visitRequestsFacade;

    private List<VisitRequests> requests;
    private List<VisitRequests> filteredRequests;
    private List<VisitRequests> selectedRequests;

    // New field to hold the request being edited via sidebar
    private VisitRequests editingRequest;

    /**
     * Loads all visit requests.
     */
    public void init() {
        requests = visitRequestsFacade.findAll();
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
                for (VisitRequests req : selectedRequests) {
                    VisitRequests managedRequest = visitRequestsFacade.find(req.getId());
                    if (managedRequest != null) {
                        visitRequestsFacade.remove(managedRequest);
                    }
                }
                selectedRequests.clear();
                init(); // Refresh the list
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

    public boolean globalFilterFunction(Object value, Object filter, Locale locale) {
        String filterText = (filter == null) ? null : filter.toString().trim().toLowerCase();
        if (filterText == null || filterText.isEmpty()) {
            return true;
        }

        VisitRequests request = (VisitRequests) value;
        return request.getUserId().getUsername().toLowerCase().contains(filterText)
                || request.getUserId().getEmail().toLowerCase().contains(filterText)
                || request.getVerificationCode().toLowerCase().contains(filterText)
                || request.getVisitDateTime().toString().toLowerCase().contains(filterText)
                || request.getPurpose().toLowerCase().contains(filterText)
                || request.getStatus().toLowerCase().contains(filterText)
                || (request.getRemarks() != null && request.getRemarks().toLowerCase().contains(filterText));
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
    }

    /**
     * Updates the edited visit request.
     */
    @Transactional
    public void updateRequest() {
        try {
            editingRequest.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
            visitRequestsFacade.edit(editingRequest);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Visit request updated successfully"));
            init(); // Refresh the list after update
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Failed to update visit request: " + e.getMessage()));
            FacesContext.getCurrentInstance().validationFailed();
        }
    }

    // --- Getters and Setters ---

    public List<VisitRequests> getRequests() {
        if (requests == null) {
            init();
        }
        return requests;
    }

    public void setRequests(List<VisitRequests> requests) {
        this.requests = requests;
    }

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
}
