package org.example.hvvs.modules.admin.service;

import org.example.hvvs.model.ManagingStaffProfiles;
import org.example.hvvs.model.ResidentProfiles;
import org.example.hvvs.model.SecurityStaffProfiles;
import org.example.hvvs.model.Users;

import java.util.List;

public interface UsersService {
    List<Users> getAllUsers();

    Users createUser(Users user);

    Users updateUser(Users user);

    void deleteUser(Users user);

    Users findByUserId(Integer userId);

    Users findByUsername(String username);

    Users findByEmail(String email);

    List<Users> findByRole(String role);

    boolean isUsernameExists(String username);

    boolean isEmailExists(String email);

    ResidentProfiles createResidentProfile(ResidentProfiles profile);

    SecurityStaffProfiles createSecurityStaffProfile(SecurityStaffProfiles profile);

    ManagingStaffProfiles createManagingStaffProfile(ManagingStaffProfiles profile);
} 