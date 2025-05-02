package com.gestion.intervention.application.piece.service;

import com.gestion.intervention.application.piece.record.PieceDTO;

import java.util.List;
import java.util.UUID;

public interface PieceService {
    PieceDTO createPiece(PieceDTO dto);
    PieceDTO getPieceById(UUID id);
    List<PieceDTO> getAllPieces();
    PieceDTO updatePiece(UUID id, PieceDTO dto);
    void deletePiece(UUID id);
}
