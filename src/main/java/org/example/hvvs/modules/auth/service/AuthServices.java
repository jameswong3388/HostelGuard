package org.example.hvvs.modules.auth.service;

import org.example.hvvs.model.User;
import org.example.hvvs.util.ServiceResult;

public interface AuthServices {

    User findUser(int id);

    ServiceResult<User> signIn(String identifier, String password, Boolean rememberMe);

    String getAutoLoginCookieValue(int id);

    int allowAutoLogin(String cookieValue);
}
