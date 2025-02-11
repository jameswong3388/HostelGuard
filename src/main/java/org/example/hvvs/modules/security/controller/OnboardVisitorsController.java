package org.example.hvvs.modules.security.controller;

import jakarta.ejb.EJB;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.example.hvvs.model.Users;
import org.example.hvvs.model.VisitRequests;
import org.example.hvvs.model.VisitorRecords;
import org.example.hvvs.modules.security.service.OnboardVisitorsService;
import org.example.hvvs.utils.CommonParam;
import org.primefaces.event.SelectEvent;
import org.primefaces.extensions.model.codescanner.Code;
import org.primefaces.model.file.UploadedFile;

import java.io.Serializable;
import java.sql.Timestamp;

@Named
@ViewScoped
public class OnboardVisitorsController implements Serializable {
    
    @EJB
    private OnboardVisitorsService securityVisitorService;
    
    private int currentStep = 1;
    private String verificationCode;
    private VisitRequests visitRequest;
    private VisitorRecords visitorRecord;
    private UploadedFile tempVisitorPhoto;
    private boolean isCheckIn = true;

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
            if (isCheckIn) {
                visitRequest = securityVisitorService.verifyVisitRequest(verificationCode);
                if (visitRequest != null) {
                    currentStep = 3;
                    FacesContext.getCurrentInstance().addMessage(null, 
                        new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Verification code is valid"));
                } else {
                    FacesContext.getCurrentInstance().addMessage(null, 
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Invalid verification code"));
                }
            } else {
                visitorRecord = securityVisitorService.findVisitorForCheckout(verificationCode);
                if (visitorRecord != null) {
                    currentStep = 3;
                    FacesContext.getCurrentInstance().addMessage(null, 
                        new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Visitor found"));
                } else {
                    FacesContext.getCurrentInstance().addMessage(null, 
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No active visitor found with this code"));
                }
            }
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getMessage()));
        }
    }

    public void nextStep() {
        if (isCheckIn && currentStep < 4) {
            currentStep++;
        } else if (!isCheckIn && currentStep < 3) {
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
            visitorRecord.setCreatedAt(new Timestamp(System.currentTimeMillis()));
            visitorRecord.setUpdatedAt(new Timestamp(System.currentTimeMillis()));

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
            if (visitorRecord == null) {
                throw new IllegalStateException("No visitor record found");
            }

            securityVisitorService.checkoutVisitor(visitorRecord);
            
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Visitor check-out completed"));
            
            restart();
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getMessage()));
        }
    }

    public Integer[] getStepSequence() {
        int totalSteps = isCheckIn ? 4 : 3;
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
} 