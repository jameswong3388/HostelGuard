package org.example.hvvs.services;


import org.example.hvvs.modules.users.entities.User;

public interface AuthServices {

    User findUser(int id);

    String signIn(String email, String password);

    String getAutoLoginCookieValue(int id);

    int allowAutoLogin(String cookieValue);
}
