package com.gestion.intervention.application.technicianinfo.service;

import com.gestion.intervention.application.technicianinfo.record.TechnicianInfoDTO;

import java.util.List;
import java.util.UUID;

public interface TechnicianInfoService {
    TechnicianInfoDTO createTechnicianInfo(TechnicianInfoDTO dto);
    TechnicianInfoDTO getTechnicianInfoById(UUID id);
    List<TechnicianInfoDTO> getAllTechnicianInfos();
    TechnicianInfoDTO updateTechnicianInfo(UUID id, TechnicianInfoDTO dto);
    void deleteTechnicianInfo(UUID id);
}
