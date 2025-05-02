package com.gestion.intervention.domain.piece.repository;

import com.gestion.intervention.domain.piece.model.Piece;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PieceRepository extends JpaRepository<Piece, UUID> {
    Optional<Piece> findByNumPieceAndMachineId(String numPiece, UUID machineId);
}
