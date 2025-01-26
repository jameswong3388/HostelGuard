package org.example.hvvs.modules.resident.controller;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.transaction.Transactional;
import org.example.hvvs.model.ResidentProfile;
import org.example.hvvs.model.User;
import org.example.hvvs.modules.resident.services.SettingsService;
import org.example.hvvs.util.CommonParam;

import java.io.Serializable;

@Named("SettingsControllerResident")
@SessionScoped
public class SettingsController implements Serializable {

    private User user;
    private ResidentProfile residentProfile;

    @Inject
    private SettingsService generalSettingsService;

    @PostConstruct
    public void init() {
        // Example: load user by some logic, e.g., from session or a dummy userId=1
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

        this.user = generalSettingsService.findUserById(currentUser.getId());
        this.residentProfile = generalSettingsService.findResidentProfileByUserId(currentUser.getId());
    }

    /**
     * Action method to save personal information
     */
    @Transactional
    public String savePersonalInformation() {
        try {
            generalSettingsService.updateUser(user);

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Success",
                            "Your personal information has been saved."));

        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Error",
                            "An error occurred while saving your information. Please try again later."));
        }

        // 5. Return to the same page or navigate to another outcome
        return null;
    }


    /* Getters and setters */
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public ResidentProfile getResidentProfile() {
        return residentProfile;
    }

    public void setResidentProfile(ResidentProfile residentProfile) {
        this.residentProfile = residentProfile;
    }
}
