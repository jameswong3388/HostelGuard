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

    private VisitRequests newRequest;
    private List<VisitRequests> userRequests;
    private ResidentProfiles residentProfile;

    @EJB
    private ResidentProfilesFacade residentProfilesFacade;

    @EJB
    private VisitRequestsFacade visitRequestsFacade;

    private List<VisitRequests> selectedRequests;
    private List<VisitRequests> filteredRequests;

    private String currentQrCode;

    public List<VisitRequests> getFilteredRequests() {
        return filteredRequests;
    }

    public void setFilteredRequests(List<VisitRequests> filteredRequests) {
        this.filteredRequests = filteredRequests;
    }

    public boolean globalFilterFunction(Object value, Object filter, Locale locale) {
        String filterText = (filter == null) ? null : filter.toString().trim().toLowerCase();
        if (filterText == null || filterText.isEmpty()) {
            return true;
        }

        VisitRequests request = (VisitRequests) value;
        return request.getVerificationCode().toLowerCase().contains(filterText)
                || request.getPurpose().toLowerCase().contains(filterText)
                || request.getStatus().toLowerCase().contains(filterText)
                || (request.getRemarks() != null && request.getRemarks().toLowerCase().contains(filterText))
                || request.getVisitDateTime().toString().toLowerCase().contains(filterText);
    }

    @PostConstruct
    public void init() {
        // Prepare the 'newRequest' object for the dialog
        this.newRequest = new VisitRequests();
        String verificationCode = UUID.randomUUID().toString();
        newRequest.setStatus("PENDING");
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

        // Load existing requests for current user
        loadUserRequests();
    }

    public VisitRequests getNewRequest() {
        return newRequest;
    }

    public void setNewRequest(VisitRequests newRequest) {
        this.newRequest = newRequest;
    }

    public List<VisitRequests> getUserRequests() {
        return userRequests;
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
    /**
     * Called in @PostConstruct or whenever you need to refresh the table data
     */
    private void loadUserRequests() {
        Users currentUser = (Users) FacesContext
                .getCurrentInstance()
                .getExternalContext()
                .getSessionMap()
                .get(CommonParam.SESSION_SELF);

        if (currentUser != null) {
            userRequests = visitRequestsFacade.findAllRequestsByUser(currentUser);
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
            Users currentUser = (Users) FacesContext
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
            visitRequestsFacade.create(newRequest);

            // Capture verification code before resetting
            this.currentQrCode = newRequest.getVerificationCode();

            // Reset the form for next time
            newRequest = new VisitRequests();
            String verificationCode = UUID.randomUUID().toString();
            newRequest.setVerificationCode(verificationCode);
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
    public void onRowEdit(RowEditEvent<VisitRequests> event) {
        try {
            VisitRequests editedRequest = event.getObject();
            editedRequest.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
            visitRequestsFacade.edit(editedRequest);

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
    public void onRowCancel(RowEditEvent<VisitRequests> event) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Cancelled", "No changes were saved"));
    }

    @Transactional
    public String cancelRequest(VisitRequests request) {
        try {
            // Mark as canceled (or any other logic you need)
            request.setStatus("CANCELLED");
            request.setRemarks("CANCELLED");
            request.setUpdatedAt(new Timestamp(System.currentTimeMillis()));

            // Persist the changes
            visitRequestsFacade.edit(request);

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
