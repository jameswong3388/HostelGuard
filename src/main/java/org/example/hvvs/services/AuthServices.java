package org.example.hvvs.services;


import org.example.hvvs.model.User;

public interface AuthServices {

    User findUser(int id);

    String signIn(String email, String password);

    String getAutoLoginCookieValue(int id);

    int allowAutoLogin(String cookieValue);
}
