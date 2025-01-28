package org.example.hvvs.modules.auth.service;

import org.example.hvvs.model.Users;
import org.example.hvvs.utils.ServiceResult;

public interface AuthServices {

    Users findUser(int id);

    ServiceResult<Users> signIn(String identifier, String password, Boolean rememberMe);

    String getAutoLoginCookieValue(int id);

    int allowAutoLogin(String cookieValue);
}
