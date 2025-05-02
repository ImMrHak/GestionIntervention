package com.gestion.intervention.application.intervention.service;

import com.gestion.intervention.application.intervention.record.InterventionDTO;

import java.util.List;
import java.util.UUID;

public interface InterventionService {
    InterventionDTO createIntervention(InterventionDTO dto);
    InterventionDTO getInterventionById(UUID id);
    List<InterventionDTO> getAllInterventions();
    InterventionDTO updateIntervention(UUID id, InterventionDTO dto);
    void deleteIntervention(UUID id);
}
