package com.gestion.intervention.application.helpdeskinfo.service;

import com.gestion.intervention.application.helpdeskinfo.record.HelpDeskInfoDTO;

import java.util.List;
import java.util.UUID;

public interface HelpDeskInfoService {
    HelpDeskInfoDTO createHelpDeskInfo(HelpDeskInfoDTO dto);
    HelpDeskInfoDTO getHelpDeskInfoById(UUID id);
    List<HelpDeskInfoDTO> getAllHelpDeskInfos();
    HelpDeskInfoDTO updateHelpDeskInfo(UUID id, HelpDeskInfoDTO dto);
    void deleteHelpDeskInfo(UUID id);
}
