package com.gestion.intervention.application.role.record;

import java.util.UUID;

public record RoleDTO(
        UUID id,
        String authority
) {}
