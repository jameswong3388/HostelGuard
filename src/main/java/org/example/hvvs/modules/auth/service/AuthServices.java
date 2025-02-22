package org.example.hvvs.modules.auth.service;

import jakarta.servlet.http.HttpServletRequest;
import org.example.hvvs.model.MfaMethods;
import org.example.hvvs.model.Users;
import org.example.hvvs.utils.ServiceResult;

import java.util.List;

public interface AuthServices {
    ServiceResult<Users> signIn(String identifier, String password, HttpServletRequest request);
    
    ServiceResult<Void> signOut(HttpServletRequest request);

    void registerSession(Users user, HttpServletRequest request);

    String redirectBasedOnRole(Users user);

    MfaMethods createMFA(Users user, MfaMethods.MfaMethodType methodType, String totpSecret, List<String> recoveryCodes);

    void disableMFA(Users user, MfaMethods.MfaMethodType methodType);

    String generateTotpSecret();

    boolean verifyTotpCode(String secret, int code);

    boolean verifySMSCode(MfaMethods method, String userInput);

    boolean verifyEmailCode(MfaMethods method, String userInput);

    boolean verifyRecoveryCode(MfaMethods method, String codeUsed);

    void sendSMSCode(MfaMethods method);

    void sendEmailCode(MfaMethods method);

    List<String> generateRecoveryCodes();
}
