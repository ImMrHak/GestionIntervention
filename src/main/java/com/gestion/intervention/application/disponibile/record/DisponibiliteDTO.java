package com.gestion.intervention.application.disponibile.record;

import java.time.LocalDateTime;
import java.util.UUID;

public record DisponibiliteDTO(
        UUID id,
        UUID technicianInfoId,
        String etat,
        LocalDateTime debut,
        LocalDateTime fin
) {}
