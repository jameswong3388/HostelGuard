package org.example.hvvs.modules.resident.controller;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import jakarta.transaction.Transactional;
import org.example.hvvs.model.*;
import org.example.hvvs.utils.CommonParam;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;
import org.primefaces.model.FilterMeta;

import java.io.Serializable;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.Map;

@Named("visitRequestsControllerResident")
@SessionScoped
public class VisitRequestsController implements Serializable {
    @EJB
    private ResidentProfilesFacade residentProfilesFacade;

    @EJB
    private VisitRequestsFacade visitRequestsFacade;

    private VisitRequests newRequest;
    private Users currentUser;
    private ResidentProfiles residentProfile;
    private List<VisitRequests> selectedRequests;
    private List<VisitRequests> filteredRequests;
    private String currentQrCode;
    private VisitRequests editingRequest;
    private LazyDataModel<VisitRequests> lazyRequestsModel;
    private String globalFilter;

    public boolean globalFilterFunction(Object value, Object filter, String filterLocale) {
        String filterText = (filter == null) ? null : filter.toString().trim().toLowerCase();
        if (filterText == null || filterText.isEmpty()) {
            return true;
        }

        VisitRequests request = (VisitRequests) value;
        return request.getVerificationCode().toLowerCase().contains(filterText)
                || request.getPurpose().toLowerCase().contains(filterText)
                || request.getStatus().toString().contains(filterText)
                || request.getRemarks().toLowerCase().contains(filterText);
    }

    @PostConstruct
    public void init() {
        currentUser = (Users) FacesContext.getCurrentInstance()
                .getExternalContext()
                .getSessionMap()
                .get(CommonParam.SESSION_SELF);
        initializeLazyModel();
        // Prepare the 'newRequest' object for the dialog
        this.newRequest = new VisitRequests();
        String verificationCode = UUID.randomUUID().toString();
        newRequest.setStatus(VisitRequests.VisitStatus.PENDING);
        newRequest.setRemarks("Awaiting approval");
        newRequest.setVerificationCode(verificationCode);

        // Get current user from session
        Users currentUser = (Users) FacesContext
                .getCurrentInstance()
                .getExternalContext()
                .getSessionMap()
                .get(CommonParam.SESSION_SELF);

        if (currentUser != null) {
            // Load resident profile
            this.residentProfile = residentProfilesFacade.find(currentUser.getId());
        }
    }

    private void initializeLazyModel() {
        lazyRequestsModel = new LazyDataModel<VisitRequests>() {
            @Override
            public int count(Map<String, FilterMeta> filterBy) {
                return 0;
            }

            @Override
            public List<VisitRequests> load(int first, int pageSize,
                                            Map<String, SortMeta> sortBy, Map<String, FilterMeta> filterBy) {

                List<VisitRequests> results = visitRequestsFacade.findRange(
                    first,
                    pageSize,
                    globalFilter,
                    sortBy,
                    currentUser
                );
                
                // Set the total count for pagination
                setRowCount(visitRequestsFacade.count(globalFilter));
                
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

    @Transactional
    public void createRequest() {
        try {
            // Basic validation
            if (newRequest.getNumberOfEntries() <= 0) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Error", "Number of entries must be greater than 0"));
                FacesContext.getCurrentInstance().validationFailed();
                return;
            }

            if (newRequest.getPurpose() == null || newRequest.getPurpose().trim().isEmpty()) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Error", "Please fill in all required fields"));
                FacesContext.getCurrentInstance().validationFailed();
                return;
            }

            // Get current user from session
            Users currentUser = (Users) FacesContext
                    .getCurrentInstance()
                    .getExternalContext()
                    .getSessionMap()
                    .get(CommonParam.SESSION_SELF);

            if (currentUser == null) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "User not authenticated"));
                FacesContext.getCurrentInstance().validationFailed();
                return;
            }

            // Set additional fields
            Timestamp now = new Timestamp(System.currentTimeMillis());
            newRequest.setUserId(currentUser);
            newRequest.setUnitNumber(residentProfile.getUnitNumber());

            // Persist
            visitRequestsFacade.create(newRequest);

            // Capture verification code before resetting
            this.currentQrCode = newRequest.getVerificationCode();

            // Reset the form for next time
            newRequest = new VisitRequests();
            String verificationCode = UUID.randomUUID().toString();
            newRequest.setVerificationCode(verificationCode);
            newRequest.setStatus(VisitRequests.VisitStatus.PENDING);
            newRequest.setRemarks("Awaiting approval");

            // Refresh the table
            lazyRequestsModel.setRowCount(visitRequestsFacade.count(globalFilter));

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Visit request created successfully"));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Error", "Failed to create visit request: " + e.getMessage()));
            FacesContext.getCurrentInstance().validationFailed();
        }
    }

    @Transactional
    public String revokeRequest() {
        try {
            // Use the editingRequest field instead of a passed parameter
            editingRequest.setStatus(VisitRequests.VisitStatus.CANCELLED);
            editingRequest.setRemarks("CANCELLED");

            // Persist the changes
            visitRequestsFacade.edit(editingRequest);
            lazyRequestsModel.setRowCount(visitRequestsFacade.count(globalFilter));

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Success",
                            "Request ID " + editingRequest.getId() + " was canceled."));

        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Error",
                            "Could not cancel request: " + e.getMessage()));
            FacesContext.getCurrentInstance().validationFailed();
        }
        return null;
    }

    @Transactional
    public void updateRequest() {
        try {
            visitRequestsFacade.edit(editingRequest);

            lazyRequestsModel.setRowCount(visitRequestsFacade.count(globalFilter));

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Visit request updated successfully"));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Failed to update visit request: " + e.getMessage()));
            FacesContext.getCurrentInstance().validationFailed();
        }
    }

    public void prepareEdit(VisitRequests request) {
        // Option 1: Edit in place. Option 2: Create a clone to edit separately.
        this.editingRequest = request;
    }

    public List<VisitRequests> getFilteredRequests() {
        return filteredRequests;
    }

    public void setFilteredRequests(List<VisitRequests> filteredRequests) {
        this.filteredRequests = filteredRequests;
    }

    public VisitRequests getNewRequest() {
        return newRequest;
    }

    public void setNewRequest(VisitRequests newRequest) {
        this.newRequest = newRequest;
    }

    public List<VisitRequests> getSelectedRequests() {
        return selectedRequests;
    }

    public void setSelectedRequests(List<VisitRequests> selectedRequests) {
        this.selectedRequests = selectedRequests;
    }

    public ResidentProfiles getResidentProfile() {
        return residentProfile;
    }

    public void setResidentProfile(ResidentProfiles residentProfile) {
        this.residentProfile = residentProfile;
    }

    public String getCurrentQrCode() {
        return currentQrCode;
    }

    public void setCurrentQrCode(String currentQrCode) {
        this.currentQrCode = currentQrCode;
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

    public String getGlobalFilter() {
        return globalFilter;
    }

    public void setGlobalFilter(String globalFilter) {
        this.globalFilter = globalFilter;
    }
}
