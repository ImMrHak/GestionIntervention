package com.gestion.intervention.application.piece.record;

import java.util.UUID;

public record PieceDTO(
        UUID id,
        String nomPiece,
        String numPiece,
        String description,
        UUID machineId
) {}
