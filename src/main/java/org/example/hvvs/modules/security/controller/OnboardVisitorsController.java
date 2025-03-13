package org.example.hvvs.modules.security.controller;

import jakarta.ejb.EJB;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import org.example.hvvs.model.Users;
import org.example.hvvs.model.VisitRequests;
import org.example.hvvs.model.VisitRequestsFacade;
import org.example.hvvs.model.VisitorRecords;
import org.example.hvvs.modules.security.service.OnboardVisitorsService;
import org.example.hvvs.utils.CommonParam;
import org.primefaces.event.SelectEvent;
import org.primefaces.extensions.model.codescanner.Code;
import org.primefaces.model.file.UploadedFile;

import java.io.Serializable;
import java.sql.Timestamp;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

@Named
@ViewScoped
public class OnboardVisitorsController implements Serializable {
    
    @EJB
    private OnboardVisitorsService securityVisitorService;

    @EJB
    private VisitRequestsFacade visitRequestsFacade;
    
    private int currentStep = 1;
    private String verificationCode;
    private VisitRequests visitRequest;
    private VisitorRecords visitorRecord;
    private UploadedFile tempVisitorPhoto;
    private boolean isCheckIn = true;
    private List<VisitorRecords> checkoutVisitors;
    private VisitorRecords selectedVisitor;
    
    private int verificationAttempts = 0;
    private final int MAX_VERIFICATION_ATTEMPTS = 3;

    public OnboardVisitorsController() {
        visitorRecord = new VisitorRecords();
    }

    public void selectCheckIn() {
        isCheckIn = true;
        currentStep = 2;
    }

    public void selectCheckOut() {
        isCheckIn = false;
        currentStep = 2;
    }

    public void onSelectOperation() {
        // Move to step 2 (verification code entry)
        currentStep = 2;
    }

    public void verifyCode() {
        try {
            // Increment attempts counter (we'll check after verification)
            verificationAttempts++;
            
            if (isCheckIn) {
                visitRequest = securityVisitorService.verifyVisitRequest(verificationCode);
                
                if (visitRequest != null) {
                    // Check if the visit request is expired (visit day should be today or in the future)
                    Date visitDay = visitRequest.getVisitDay();
                    LocalDate today = LocalDate.now();
                    LocalDate visitDate = visitDay.toLocalDate();
                    
                    if (visitDate.isBefore(today)) {
                        // The visit request has expired
                        FacesContext.getCurrentInstance().addMessage(null,
                                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", 
                                "This visit request has expired or the visit day has passed."));
                        visitRequest = null;
                        return;
                    }

                    // Auto-populate visitor information from the visit request
                    visitorRecord.setVisitorName(visitRequest.getVisitorName());
                    visitorRecord.setVisitorIc(visitRequest.getVisitorIdentity());

                    currentStep = 3;
                    verificationAttempts = 0; // Reset attempts on success
                    FacesContext.getCurrentInstance().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Verification successful"));
                } else {
                    // Check if we've reached max attempts
                    checkMaxAttemptsAndHandle();
                }
            } else {
                checkoutVisitors = securityVisitorService.findVisitorsForCheckout(verificationCode);
                if (!checkoutVisitors.isEmpty()) {
                    // Skip the selection step and automatically select the first visitor
                    if (checkoutVisitors.size() == 1) {
                        // Only one visitor to check out - straightforward case
                        selectedVisitor = checkoutVisitors.get(0);
                        currentStep = 3;
                        FacesContext.getCurrentInstance().addMessage(null,
                                new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Visitor found for checkout"));
                    } else {
                        // Multiple visitors - select the first one but inform the user
                        selectedVisitor = checkoutVisitors.get(0);
                        currentStep = 3;
                        FacesContext.getCurrentInstance().addMessage(null,
                                new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", 
                                "Multiple visitors found - showing the first one. You'll need to check out others separately."));
                    }
                    verificationAttempts = 0; // Reset attempts on success
                } else {
                    // Check if we've reached max attempts
                    checkMaxAttemptsAndHandle();
                }
            }
        } catch (Exception e) {
            // Check if we've reached max attempts
            checkMaxAttemptsAndHandle();
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getMessage()));
        }
    }
    
    private void checkMaxAttemptsAndHandle() {
        int remainingAttempts = MAX_VERIFICATION_ATTEMPTS - verificationAttempts;
        
        if (remainingAttempts <= 0) {
            // Max attempts reached, redirect to home page
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", 
                "Maximum verification attempts reached."));
            
            // Reset everything
            restart();
        } else {
            // Still have attempts left
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", 
                "Invalid verification code. Attempts remaining: " + remainingAttempts));
        }
    }

    public void nextStep() {
        if (isCheckIn && currentStep < 4) {
            currentStep++;
        } else if (!isCheckIn && currentStep < 3) { // Check-out now has only 3 steps
            currentStep++;
        }
    }

    public void previousStep() {
        if (currentStep > 1) {
            currentStep--;
        }
    }

    public void restart() {
        currentStep = 1;
        verificationCode = null;
        visitRequest = null;
        visitorRecord = new VisitorRecords();
        tempVisitorPhoto = null;
        isCheckIn = true;
        verificationAttempts = 0;
    }

    public void completeRegistration() {
        try {
            if (visitRequest == null) {
                throw new IllegalStateException("No valid visit request found");
            }

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

            visitorRecord.setRequestId(visitRequest);
            visitorRecord.setSecurityStaffId(currentUser);
            visitorRecord.setCheckInTime(new Timestamp(System.currentTimeMillis()));

            securityVisitorService.registerVisitor(visitorRecord, tempVisitorPhoto);
            
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Visitor check-in completed"));
            
            restart();
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getMessage()));
        }
    }

    public void completeCheckout() {
        try {
            if (selectedVisitor == null) {
                throw new IllegalStateException("No visitor record found");
            }

            securityVisitorService.checkoutVisitor(selectedVisitor);
            
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Visitor check-out completed"));
            
            restart();
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getMessage()));
        }
    }

    public Integer[] getStepSequence() {
        int totalSteps = isCheckIn ? 4 : 3; // 4 steps for check-in, 3 steps for check-out
        Integer[] sequence = new Integer[totalSteps];
        for (int i = 0; i < totalSteps; i++) {
            sequence[i] = i + 1;
        }
        return sequence;
    }

    public void onCodeScanned(final SelectEvent<Code> event) {
        final Code code = event.getObject();
        this.verificationCode = code.getValue();
        FacesContext.getCurrentInstance()
            .addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "QR code scanned successfully"));
    }

    // Getters and Setters
    public int getCurrentStep() {
        return currentStep;
    }

    public void setCurrentStep(int currentStep) {
        this.currentStep = currentStep;
    }

    public String getVerificationCode() {
        return verificationCode;
    }

    public void setVerificationCode(String verificationCode) {
        this.verificationCode = verificationCode;
    }

    public VisitorRecords getVisitorRecord() {
        return visitorRecord;
    }

    public void setVisitorRecord(VisitorRecords visitorRecord) {
        this.visitorRecord = visitorRecord;
    }

    public UploadedFile getTempVisitorPhoto() {
        return tempVisitorPhoto;
    }

    public void setTempVisitorPhoto(UploadedFile tempVisitorPhoto) {
        this.tempVisitorPhoto = tempVisitorPhoto;
    }

    public boolean isCheckIn() {
        return isCheckIn;
    }

    public void setCheckIn(boolean checkIn) {
        isCheckIn = checkIn;
    }

    public VisitorRecords getSelectedVisitor() {
        return selectedVisitor;
    }

    public void setSelectedVisitor(VisitorRecords selectedVisitor) {
        this.selectedVisitor = selectedVisitor;
    }

    public List<VisitorRecords> getCheckoutVisitors() {
        return checkoutVisitors;
    }

    public void setCheckoutVisitors(List<VisitorRecords> checkoutVisitors) {
        this.checkoutVisitors = checkoutVisitors;
    }

    public VisitRequests getVisitRequest() {
        return visitRequest;
    }

    public void setVisitRequest(VisitRequests visitRequest) {
        this.visitRequest = visitRequest;
    }
}