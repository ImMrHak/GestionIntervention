package com.gestion.intervention.application.disponibile.service;

import com.gestion.intervention.application.disponibile.record.DisponibiliteDTO;

import java.util.List;
import java.util.UUID;

public interface DisponibiliteService {
    DisponibiliteDTO createDisponibilite(DisponibiliteDTO dto);
    DisponibiliteDTO getDisponibiliteById(UUID id);
    List<DisponibiliteDTO> getAllDisponibilites();
    DisponibiliteDTO updateDisponibilite(UUID id, DisponibiliteDTO dto);
    void deleteDisponibilite(UUID id);
}
