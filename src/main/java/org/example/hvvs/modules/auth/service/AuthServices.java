package org.example.hvvs.modules.auth.service;

import org.example.hvvs.model.Users;
import org.example.hvvs.utils.ServiceResult;

public interface AuthServices {
    ServiceResult<Users> signIn(String identifier, String password);
}
