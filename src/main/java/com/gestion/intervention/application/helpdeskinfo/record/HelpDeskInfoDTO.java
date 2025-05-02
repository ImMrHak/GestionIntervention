package com.gestion.intervention.application.helpdeskinfo.record;

import java.util.UUID;

public record HelpDeskInfoDTO(
        UUID id,
        UUID personId,
        UUID steId
) {}