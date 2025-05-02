package com.gestion.intervention.application.panne.record;

import java.util.UUID;

public record PanneDTO(
        UUID id,
        String typePanne,
        UUID machineId,
        UUID reporterId
) {}