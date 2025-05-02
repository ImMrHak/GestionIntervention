package com.gestion.intervention.application.person.record;

import java.time.LocalDate;
import java.util.UUID;

public record PersonDTO(
        UUID id,
        String CIN,
        String nom,
        String prenom,
        String email,
        String username,
        String password,
        String telephone,
        String address,
        LocalDate dateNaissance
) {}

