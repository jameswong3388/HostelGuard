package org.example.hvvs.modules.auth.service;

import org.example.hvvs.model.MfaMethods;
import org.example.hvvs.model.Users;
import org.example.hvvs.utils.CommonParam;
import org.example.hvvs.utils.ServiceResult;

import java.util.List;

public interface AuthServices {
    ServiceResult<Users> signIn(String identifier, String password);

    void registerSession(Users user);

    String redirectBasedOnRole(Users user);

    MfaMethods createMFA(Users user, MfaMethods.MfaMethodType methodType, String totpSecret, List<String> recoveryCodes);

    void disableMFA(Users user, MfaMethods.MfaMethodType methodType);

    String generateTotpSecret();

    boolean verifyTotpCode(String secret, int code);

    ServiceResult<Void> sendSMSCode(MfaMethods method);

    ServiceResult<Void> sendEmailCode(MfaMethods method);

    boolean verifyEmailCode(MfaMethods method, String userInput);

    boolean verifyRecoveryCode(MfaMethods method, String codeUsed);

    List<String> generateRecoveryCodes();
}
