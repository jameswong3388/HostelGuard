package org.example.hvvs.modules.admin.service;

import org.example.hvvs.model.User;
import org.example.hvvs.model.ResidentProfile;
import org.example.hvvs.model.SecurityStaffProfile;
import org.example.hvvs.model.ManagingStaffProfile;

import java.util.List;

public interface UsersService {
    List<User> getAllUsers();

    User createUser(User user);

    User updateUser(User user);

    void deleteUser(User user);

    User findByUsername(String username);

    User findByEmail(String email);

    List<User> findByRole(String role);

    boolean isUsernameExists(String username);

    boolean isEmailExists(String email);

    ResidentProfile createResidentProfile(ResidentProfile profile);

    SecurityStaffProfile createSecurityStaffProfile(SecurityStaffProfile profile);

    ManagingStaffProfile createManagingStaffProfile(ManagingStaffProfile profile);
} 