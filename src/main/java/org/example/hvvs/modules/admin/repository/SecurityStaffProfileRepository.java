package org.example.hvvs.modules.admin.repository;

import org.example.hvvs.model.SecurityStaffProfiles;
import java.util.List;

public interface SecurityStaffProfileRepository {
    List<SecurityStaffProfiles> findAll();
} 