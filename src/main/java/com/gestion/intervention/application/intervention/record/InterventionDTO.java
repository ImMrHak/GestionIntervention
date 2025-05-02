package com.gestion.intervention.application.intervention.record;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

public record InterventionDTO(
        UUID id,
        LocalDateTime dateDebut,
        LocalDateTime dateFin,
        Duration duree,
        UUID technicianId,
        UUID panneId
) {}
