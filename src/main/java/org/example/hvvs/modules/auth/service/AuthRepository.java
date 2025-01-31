package org.example.hvvs.modules.auth.service;

import org.example.hvvs.model.MfaMethods;
import org.example.hvvs.model.Users;

import java.util.List;

public interface AuthRepository {
    MfaMethods findPrimaryMfaMethodByUser(Users user);

    List<MfaMethods> findMfaMethodByUser(Users user);
}
