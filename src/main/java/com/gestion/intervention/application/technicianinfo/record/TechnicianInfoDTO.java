package com.gestion.intervention.application.technicianinfo.record;

import java.util.UUID;

public record TechnicianInfoDTO(
        UUID id,
        UUID personId,
        String idSte,
        String specialite,
        int nbrPanne,
        int nbrPanneRegle
) {}
