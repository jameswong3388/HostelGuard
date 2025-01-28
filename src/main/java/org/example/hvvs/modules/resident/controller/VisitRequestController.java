package org.example.hvvs.modules.resident.controller;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.transaction.Transactional;
import org.example.hvvs.model.VisitRequest;
import org.example.hvvs.model.User;
import org.example.hvvs.model.ResidentProfile;
import org.example.hvvs.modules.resident.services.VisitRequestService;
import org.example.hvvs.modules.resident.services.SettingsServiceResident;
import org.example.hvvs.utils.CommonParam;
import org.primefaces.event.RowEditEvent;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Named("visitRequestControllerResident")
@SessionScoped
public class VisitRequestController implements Serializable {

    private VisitRequest newRequest;
    private List<VisitRequest> userRequests;
    private ResidentProfile residentProfile;

    @Inject
    private VisitRequestService visitRequestService;

    @Inject
    private SettingsServiceResident settingsService;

    private List<VisitRequest> selectedRequests;
    private List<VisitRequest> filteredRequests;

    public List<VisitRequest> getFilteredRequests() {
        return filteredRequests;
    }

    public void setFilteredRequests(List<VisitRequest> filteredRequests) {
        this.filteredRequests = filteredRequests;
    }

    public boolean globalFilterFunction(Object value, Object filter, Locale locale) {
        String filterText = (filter == null) ? null : filter.toString().trim().toLowerCase();
        if (filterText == null || filterText.isEmpty()) {
            return true;
        }

        VisitRequest request = (VisitRequest) value;
        return request.getVerificationCode().toLowerCase().contains(filterText)
                || request.getPurpose().toLowerCase().contains(filterText)
                || request.getStatus().toLowerCase().contains(filterText)
                || (request.getRemarks() != null && request.getRemarks().toLowerCase().contains(filterText))
                || request.getVisitDateTime().toString().toLowerCase().contains(filterText);
    }

    @PostConstruct
    public void init() {
        // Prepare the 'newRequest' object for the dialog
        this.newRequest = new VisitRequest();
        String verificationCode = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        newRequest.setStatus("PENDING");
        newRequest.setRemarks("Awaiting approval");
        newRequest.setVerificationCode(verificationCode);

        // Get current user from session
        User currentUser = (User) FacesContext
                .getCurrentInstance()
                .getExternalContext()
                .getSessionMap()
                .get(CommonParam.SESSION_SELF);

        if (currentUser != null) {
            // Load resident profile
            this.residentProfile = settingsService.findResidentProfileByUserId(currentUser.getId());
        }

        // Load existing requests for current user
        loadUserRequests();
    }

    public VisitRequest getNewRequest() {
        return newRequest;
    }

    public void setNewRequest(VisitRequest newRequest) {
        this.newRequest = newRequest;
    }

    public List<VisitRequest> getUserRequests() {
        return userRequests;
    }

    public List<VisitRequest> getSelectedRequests() {
        return selectedRequests;
    }

    public void setSelectedRequests(List<VisitRequest> selectedRequests) {
        this.selectedRequests = selectedRequests;
    }

    public ResidentProfile getResidentProfile() {
        return residentProfile;
    }

    public void setResidentProfile(ResidentProfile residentProfile) {
        this.residentProfile = residentProfile;
    }

    /**
     * Called in @PostConstruct or whenever you need to refresh the table data
     */
    private void loadUserRequests() {
        User currentUser = (User) FacesContext
                .getCurrentInstance()
                .getExternalContext()
                .getSessionMap()
                .get(CommonParam.SESSION_SELF);

        if (currentUser != null) {
            userRequests = visitRequestService.findRequestsByUserEntity(currentUser);
        } else {
            userRequests = new ArrayList<>();
        }
    }

    @Transactional
    public void createRequest() {
        try {
            // Basic validation
            if (newRequest.getPurpose() == null || newRequest.getPurpose().trim().isEmpty()) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Error", "Please fill in all required fields"));
                return;
            }
            // Get current user from session
            User currentUser = (User) FacesContext
                    .getCurrentInstance()
                    .getExternalContext()
                    .getSessionMap()
                    .get(CommonParam.SESSION_SELF);

            if (currentUser == null) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "User not authenticated"));
                return;
            }

            // Set additional fields
            Timestamp now = new Timestamp(System.currentTimeMillis());
            newRequest.setUserId(currentUser);
            newRequest.setUnitNumber(residentProfile.getUnitNumber());
            newRequest.setCreatedAt(now);
            newRequest.setUpdatedAt(now);

            // Persist
            visitRequestService.create(newRequest);

            // Reset the form for next time
            newRequest = new VisitRequest();
            newRequest.setVerificationCode(UUID.randomUUID().toString().substring(0, 6).toUpperCase());
            newRequest.setStatus("PENDING");
            newRequest.setRemarks("Awaiting approval");

            // Reload table
            loadUserRequests();

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Visit request created successfully"));

        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Error", "Failed to create visit request: " + e.getMessage()));
        }
    }

    /**
     * Called by PrimeFaces rowEdit event when a row is saved (after inline editing).
     */
    @Transactional
    public void onRowEdit(RowEditEvent<VisitRequest> event) {
        try {
            VisitRequest editedRequest = event.getObject();
            editedRequest.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
            visitRequestService.update(editedRequest);

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Request updated"));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Could not update request: " + e.getMessage()));
        }
    }

    /**
     * Called by PrimeFaces rowEdit event when a row edit is canceled.
     */
    public void onRowCancel(RowEditEvent<VisitRequest> event) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Cancelled", "No changes were saved"));
    }

    @Transactional
    public String cancelRequest(VisitRequest request) {
        try {
            // Mark as canceled (or any other logic you need)
            request.setStatus("CANCELLED");
            request.setRemarks("CANCELLED");
            request.setUpdatedAt(new Timestamp(System.currentTimeMillis()));

            // Persist the changes
            visitRequestService.update(request);

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Success",
                            "Request ID " + request.getId() + " was canceled."));

        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Error",
                            "Could not cancel request: " + e.getMessage()));
        }

        // return null to stay on the same page
        return null;
    }
}
