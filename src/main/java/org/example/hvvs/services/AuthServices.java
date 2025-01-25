package org.example.hvvs.services;


import org.example.hvvs.model.User;
import org.example.hvvs.util.ServiceResult;

public interface AuthServices {

    User findUser(int id);

    ServiceResult<User> signIn(String email, String password, Boolean rememberMe);

    String getAutoLoginCookieValue(int id);

    int allowAutoLogin(String cookieValue);
}
