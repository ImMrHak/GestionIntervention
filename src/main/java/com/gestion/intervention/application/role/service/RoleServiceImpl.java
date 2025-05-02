package com.gestion.intervention.application.role.service;

import com.gestion.intervention.application.role.record.RoleDTO;
import com.gestion.intervention.domain.role.model.Role;
import com.gestion.intervention.domain.role.repository.RoleRepository;
import com.gestion.intervention.domain.person.repository.PersonRepository; // Added import (assuming Person has Roles)

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Added import

import java.util.List;
import java.util.Optional; // Added import
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final PersonRepository personRepository; // Added dependency (assuming Person links to Role)

    @Override
    @Transactional
    public RoleDTO createRole(RoleDTO dto) {
        validateRoleAuthority(dto.authority());
        checkDuplicateRoleAuthority(dto.authority(), null); // Check uniqueness

        Role entity = Role.builder()
                // .id(dto.id()) // ID is usually auto-generated, remove if using DB generation
                .authority(formatAuthority(dto.authority())) // Format authority if needed
                .build();

        Role savedEntity = roleRepository.save(entity);
        return toDto(savedEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public RoleDTO getRoleById(UUID id) {
        return roleRepository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new EntityNotFoundException("Role not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoleDTO> getAllRoles() {
        return roleRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public RoleDTO updateRole(UUID id, RoleDTO dto) {
        Role entity = roleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Role not found with id: " + id));

        validateRoleAuthority(dto.authority());

        // Check uniqueness only if the authority is actually changing
        if (!entity.getAuthority().equalsIgnoreCase(dto.authority())) {
            checkDuplicateRoleAuthority(dto.authority(), id);
        }

        entity.setAuthority(formatAuthority(dto.authority())); // Format potentially new authority

        Role updatedEntity = roleRepository.save(entity);
        return toDto(updatedEntity);
    }

    @Override
    @Transactional
    public void deleteRole(UUID id) {
        if (!roleRepository.existsById(id)) {
            throw new EntityNotFoundException("Role not found with id: " + id);
        }

        // Check if any Person is assigned this role before deleting
        checkRoleAssociations(id);

        roleRepository.deleteById(id);
    }

    private void validateRoleAuthority(String authority) {
        if (authority == null || authority.trim().isEmpty()) {
            throw new IllegalArgumentException("Role authority cannot be null or empty.");
        }
        // Optional: Add specific format checks (e.g., must start with "ROLE_")
        // if (!authority.startsWith("ROLE_")) {
        //     throw new IllegalArgumentException("Role authority must start with 'ROLE_'.");
        // }
    }

    // Optional: Standardize authority format (e.g., uppercase, add prefix)
    private String formatAuthority(String authority) {
        if (authority == null) return null;
        String formatted = authority.trim().toUpperCase();
        // Example: Ensure ROLE_ prefix
        // if (!formatted.startsWith("ROLE_")) {
        //     formatted = "ROLE_" + formatted;
        // }
        return formatted;
    }

    // Requires custom query in RoleRepository
    private void checkDuplicateRoleAuthority(String authority, UUID roleIdToExclude) {
        String formattedAuthority = formatAuthority(authority); // Use formatted authority for check
        Optional<Role> existingRole = roleRepository.findByAuthority(formattedAuthority); // Find by formatted name

        if (existingRole.isPresent() && (roleIdToExclude == null || !existingRole.get().getId().equals(roleIdToExclude))) {
            throw new IllegalArgumentException("A role with authority '" + formattedAuthority + "' already exists.");
        }
    }

    // Requires custom query in PersonRepository (or intermediate join table repository)
    private void checkRoleAssociations(UUID roleId) {
        // Assuming Person has a many-to-many relationship with Role and
        // PersonRepository has a method to count persons by roleId.
        long userCount = personRepository.countByRoleId(roleId); // Adjust method name as needed
        if (userCount > 0) {
            throw new IllegalStateException("Cannot delete role with id: " + roleId + " because it is assigned to " + userCount + " user(s).");
        }
    }


    private RoleDTO toDto(Role entity) {
        if (entity == null) {
            return null;
        }
        return new RoleDTO(
                entity.getId(),
                entity.getAuthority()
        );
    }
}