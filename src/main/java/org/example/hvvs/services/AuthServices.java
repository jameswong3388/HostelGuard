package org.example.hvvs.services;

import org.example.hvvs.model.User;

public interface AuthServices {
    User signIn(String email, String password);
}
