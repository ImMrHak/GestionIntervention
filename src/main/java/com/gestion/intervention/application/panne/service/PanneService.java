package com.gestion.intervention.application.panne.service;

import com.gestion.intervention.application.panne.record.PanneDTO;

import java.util.List;
import java.util.UUID;

public interface PanneService {
    PanneDTO createPanne(PanneDTO dto);
    PanneDTO getPanneById(UUID id);
    List<PanneDTO> getAllPannes();
    PanneDTO updatePanne(UUID id, PanneDTO dto);
    void deletePanne(UUID id);
}
