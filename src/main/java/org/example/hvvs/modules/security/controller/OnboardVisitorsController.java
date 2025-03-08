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
                    // Check if the visit request is expired (24 hours after scheduled time)
                    Timestamp visitTime = visitRequest.getVisitDateTime();
                    Timestamp currentTime = new Timestamp(System.currentTimeMillis());
                    Timestamp expirationTime = new Timestamp(visitTime.getTime() + (24 * 60 * 60 * 1000)); // 24 hours
                    
                    if (currentTime.after(expirationTime)) {
                        // The visit request has expired
                        FacesContext.getCurrentInstance().addMessage(null,
                                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", 
                                "This visit request has expired."));
                        visitRequest = null;
                        return;
                    }

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
                    currentStep = 3;
                    verificationAttempts = 0; // Reset attempts on success
                    FacesContext.getCurrentInstance().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Visitor(s) found"));
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
        if ((isCheckIn && currentStep < 4) || (!isCheckIn && currentStep < 4)) {
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
            
            // Decrement number_of_entries after successful check-in
            int remainingEntries = visitRequest.getNumberOfEntries() - 1;
            visitRequest.setNumberOfEntries(remainingEntries);
            visitRequestsFacade.edit(visitRequest);

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
        int totalSteps = 4;
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
        nextStep();
    }

    public List<VisitorRecords> getCheckoutVisitors() {
        return checkoutVisitors;
    }

    public void setCheckoutVisitors(List<VisitorRecords> checkoutVisitors) {
        this.checkoutVisitors = checkoutVisitors;
    }
}