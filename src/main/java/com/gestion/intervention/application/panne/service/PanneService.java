package com.gestion.intervention.application.panne.service;

import com.gestion.intervention.application.panne.record.PanneDTO;
import com.gestion.intervention.domain.panne.enumeration.PanneStatus;

import java.util.List;
import java.util.UUID;

public interface PanneService {
    PanneDTO createPanne(PanneDTO dto);
    PanneDTO getPanneById(UUID id);
    List<PanneDTO> getAllPannes();
    PanneDTO updatePanne(UUID id, PanneDTO dto);
    void deletePanne(UUID id);
    Integer getTotalPannesByStatus(PanneStatus status, UUID userId);
    Integer getTotalPannesByReporterId(UUID reporterId);
}
