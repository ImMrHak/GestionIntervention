package com.gestion.intervention.application.role.service;

import com.gestion.intervention.application.role.record.RoleDTO;

import java.util.List;
import java.util.UUID;

public interface RoleService {
    RoleDTO createRole(RoleDTO dto);
    RoleDTO getRoleById(UUID id);
    List<RoleDTO> getAllRoles();
    RoleDTO updateRole(UUID id, RoleDTO dto);
    void deleteRole(UUID id);
}
