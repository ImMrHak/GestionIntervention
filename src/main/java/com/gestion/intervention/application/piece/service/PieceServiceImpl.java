package com.gestion.intervention.application.piece.service;

import com.gestion.intervention.application.piece.record.PieceDTO;
import com.gestion.intervention.domain.machine.model.Machine;
import com.gestion.intervention.domain.machine.repository.MachineRepository; // Added import
import com.gestion.intervention.domain.piece.model.Piece;
import com.gestion.intervention.domain.piece.repository.PieceRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Added import

import java.util.List;
import java.util.Optional; // Added import
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PieceServiceImpl implements PieceService {

    private final PieceRepository pieceRepository;
    private final MachineRepository machineRepository; // Added dependency

    @Override
    @Transactional
    public PieceDTO createPiece(PieceDTO dto) {
        validatePieceInput(dto);

        Machine machine = fetchMachine(dto.machineId());

        // Optional: Check for duplicate piece number within the same machine
        checkForDuplicatePieceNum(dto.numPiece(), dto.machineId(), null);

        Piece piece = Piece.builder()
                .nomPiece(dto.nomPiece())
                .numPiece(dto.numPiece()) // Added numPiece
                .description(dto.description())
                .machine(machine) // Use fetched machine
                .build();

        Piece savedPiece = pieceRepository.save(piece);
        return toDto(savedPiece);
    }

    @Override
    @Transactional(readOnly = true)
    public PieceDTO getPieceById(UUID id) {
        return pieceRepository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new EntityNotFoundException("Piece not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PieceDTO> getAllPieces() {
        return pieceRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PieceDTO updatePiece(UUID id, PieceDTO dto) {
        Piece piece = pieceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Piece not found with id: " + id));

        validatePieceInput(dto); // Validate new data

        Machine machine = fetchMachine(dto.machineId()); // Fetch potentially new machine

        // Check for duplicates only if numPiece or machine changes
        boolean needsDuplicateCheck = (dto.numPiece() != null && !dto.numPiece().equals(piece.getNumPiece())) ||
                !dto.machineId().equals(piece.getMachine().getId());

        if (needsDuplicateCheck) {
            checkForDuplicatePieceNum(dto.numPiece(), dto.machineId(), id);
        }

        piece.setNomPiece(dto.nomPiece());
        piece.setNumPiece(dto.numPiece());
        piece.setDescription(dto.description());
        piece.setMachine(machine); // Update machine association

        Piece updatedPiece = pieceRepository.save(piece);
        return toDto(updatedPiece);
    }

    @Override
    @Transactional
    public void deletePiece(UUID id) {
        if (!pieceRepository.existsById(id)) {
            throw new EntityNotFoundException("Piece not found with id: " + id);
        }
        // Optional: Add check here if Piece is referenced in other tables (e.g., InterventionParts)
        // checkPieceAssociations(id);

        pieceRepository.deleteById(id);
    }

    private void validatePieceInput(PieceDTO dto) {
        if (dto.machineId() == null) {
            throw new IllegalArgumentException("Machine ID cannot be null for a piece.");
        }
        if (dto.nomPiece() == null || dto.nomPiece().trim().isEmpty()) {
            throw new IllegalArgumentException("Piece name (nomPiece) cannot be null or empty.");
        }
        // Add validation for numPiece if it's mandatory or has format rules
        if (dto.numPiece() == null || dto.numPiece().trim().isEmpty()) {
            throw new IllegalArgumentException("Piece number (numPiece) cannot be null or empty.");
        }
    }

    private Machine fetchMachine(UUID machineId) {
        if (machineId == null) {
            // This case is already handled by validatePieceInput, but added for safety
            throw new IllegalArgumentException("Machine ID cannot be null.");
        }
        return machineRepository.findById(machineId)
                .orElseThrow(() -> new EntityNotFoundException("Machine not found with id: " + machineId));
    }

    // Optional: Check if numPiece is unique for the given machineId
    // Requires custom query in PieceRepository
    private void checkForDuplicatePieceNum(String numPiece, UUID machineId, UUID pieceIdToExclude) {
        if (numPiece == null || numPiece.trim().isEmpty()) {
            return; // Don't check uniqueness if numPiece is not provided or empty
        }

        Optional<Piece> existingPiece = pieceRepository
                .findByNumPieceAndMachineId(numPiece, machineId); // Requires this method in repository

        if (existingPiece.isPresent() && (pieceIdToExclude == null || !existingPiece.get().getId().equals(pieceIdToExclude))) {
            throw new IllegalArgumentException("A piece with number '" + numPiece + "' already exists for machine with id: " + machineId);
        }
    }

    /*
    // Optional: Check associations before deletion
    private void checkPieceAssociations(UUID pieceId) {
        // Example: Check if this piece is part of any non-completed Intervention records
        // if (interventionPartRepository.existsActiveByPieceId(pieceId)) { // Requires InterventionPart entity/repo
        //     throw new IllegalStateException("Cannot delete piece with id: " + pieceId + " as it is used in active interventions.");
        // }
    }
    */


    private PieceDTO toDto(Piece piece) {
        if (piece == null) {
            return null;
        }
        // Ensure machine is not null before accessing its ID
        UUID machineId = (piece.getMachine() != null) ? piece.getMachine().getId() : null;

        // Handle case where machine might be null (though FK constraint should prevent this)
        if (machineId == null) {
            // Log a warning or handle as appropriate
            System.err.println("Warning: Piece with ID " + piece.getId() + " has a null machine association.");
        }

        return new PieceDTO(
                piece.getId(),
                piece.getNomPiece(),
                piece.getNumPiece(),
                piece.getDescription(),
                machineId // Use the safely retrieved machineId
        );
    }
}