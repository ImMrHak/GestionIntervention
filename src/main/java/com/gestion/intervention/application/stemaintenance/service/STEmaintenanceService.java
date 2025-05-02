package com.gestion.intervention.application.stemaintenance.service;

import com.gestion.intervention.application.stemaintenance.record.STEmaintenanceDTO;

import java.util.List;
import java.util.UUID;

public interface STEmaintenanceService {
    STEmaintenanceDTO createSTEmaintenance(STEmaintenanceDTO dto);
    STEmaintenanceDTO getSTEmaintenanceById(UUID id);
    List<STEmaintenanceDTO> getAllSTEmaintenances();
    STEmaintenanceDTO updateSTEmaintenance(UUID id, STEmaintenanceDTO dto);
    void deleteSTEmaintenance(UUID id);
}
