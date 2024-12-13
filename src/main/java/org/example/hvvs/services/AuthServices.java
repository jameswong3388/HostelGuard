package org.example.hvvs.services;


import org.example.hvvs.model.User;

public interface AuthServices {

    User findUser(String id);

    String signIn(String email, String password);

    String getAutoLoginCookieValue(String id);

    String allowAutoLogin(String cookieValue);
}
