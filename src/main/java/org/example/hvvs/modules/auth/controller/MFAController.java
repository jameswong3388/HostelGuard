package org.example.hvvs.modules.auth.controller;

import jakarta.ejb.EJB;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import org.example.hvvs.model.MfaMethods;
import org.example.hvvs.model.Users;
import org.example.hvvs.modules.auth.service.AuthRepository;
import org.example.hvvs.modules.auth.service.AuthServices;
import org.example.hvvs.utils.CommonParam;

import java.io.Serializable;

@Named("mfaController")
@RequestScoped
public class MFAController implements Serializable {

    @EJB
    private AuthServices authServices;

    @EJB
    private AuthRepository authRepository;

    private String code; // code the user inputs

    public String verifyMfa() {
        // Retrieve the user from pre-auth session
        FacesContext ctx = FacesContext.getCurrentInstance();
        Users preAuthUser = (Users) ctx.getExternalContext()
                .getSessionMap().get(CommonParam.PRE_AUTH_USER);

        if (preAuthUser == null) {
            ctx.addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_ERROR, "Error", "Pls login first!"));
            return "/auth.xhtml?faces-redirect=true";
        }

        // 1) Find the user's primary method from DB
        MfaMethods primaryMethod = authRepository.findPrimaryMfaMethodByUser(preAuthUser);

        if (primaryMethod == null) {
            ctx.addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_ERROR, "Error", "There is no primary mfa method associated with the user."));
            return "/auth.xhtml?faces-redirect=true";
        }

        boolean valid = false;
        if (primaryMethod.getMethod() == MfaMethods.MfaMethodType.TOTP) {
            // Convert user input code to int
            int codeVal = Integer.parseInt(code);
            valid = authServices.verifyTotpCode(primaryMethod.getSecret(), codeVal);
        }
        else if (primaryMethod.getMethod() == MfaMethods.MfaMethodType.SMS) {
            valid = true;
        }
        else if (primaryMethod.getMethod() == MfaMethods.MfaMethodType.EMAIL) {
            valid = true;
        }
        else if (primaryMethod.getMethod() == MfaMethods.MfaMethodType.RECOVERY_CODES) {
            valid = true;
        }

        if (!valid) {
            System.out.println("3");
            ctx.addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_ERROR, "Error", "MFA code is invalid."));
            return null; // stay on the same page
        }

        authServices.registerSession(preAuthUser);
        ctx.getExternalContext().getSessionMap().remove("PRE_AUTH_USER");

        return authServices.redirectBasedOnRole(preAuthUser);
    }

    // getters/setters
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
}
