package com.gestion.intervention.application.machine.record;

import java.util.UUID;

public record MachineDTO(
        UUID id,
        String type,
        String etat
) {}