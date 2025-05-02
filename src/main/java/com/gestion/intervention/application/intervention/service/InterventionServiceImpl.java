package com.gestion.intervention.application.intervention.service;

import com.gestion.intervention.application.intervention.record.InterventionDTO;
import com.gestion.intervention.domain.intervention.model.Intervention;
import com.gestion.intervention.domain.intervention.repository.InterventionRepository;
import com.gestion.intervention.domain.panne.model.Panne;
import com.gestion.intervention.domain.panne.repository.PanneRepository;
import com.gestion.intervention.domain.technicianinfo.model.TechnicianInfo;
import com.gestion.intervention.domain.technicianinfo.repository.TechnicianInfoRepository;
// Potentially needed for availability checks
// import com.gestion.intervention.domain.disponibile.repository.DisponibiliteRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime; // Assuming dates are LocalDateTime
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InterventionServiceImpl implements InterventionService {

    private final InterventionRepository interventionRepository;
    private final TechnicianInfoRepository technicianInfoRepository;
    private final PanneRepository panneRepository;
    // Optional: Inject DisponibiliteRepository if you need to check technician availability based on Disponibilite entities
    // private final DisponibiliteRepository disponibiliteRepository;

    @Transactional
    @Override
    public InterventionDTO createIntervention(InterventionDTO dto) {
        validateInterventionDates(dto.dateDebut(), dto.dateFin());

        TechnicianInfo technicianInfo = fetchTechnicianIfPresent(dto.technicianId());
        Panne panne = fetchPanneIfPresent(dto.panneId());

        // Optional but recommended: Check if Panne is in a state that requires intervention
        // validatePanneStatus(panne); // Implement this based on Panne model's status field

        // Optional but recommended: Check for conflicting interventions for the same technician
        if (technicianInfo != null && dto.dateDebut() != null && dto.dateFin() != null) {
            checkForConflictingInterventions(technicianInfo.getId(), dto.dateDebut(), dto.dateFin(), null);
        }

        // Optional: Check if technician is generally available during this period (more complex)
        // if (technicianInfo != null && dto.dateDebut() != null && dto.dateFin() != null) {
        //     checkTechnicianAvailability(technicianInfo.getId(), dto.dateDebut(), dto.dateFin());
        // }

        Intervention intervention = Intervention.builder()
                .dateDebut(dto.dateDebut())
                .dateFin(dto.dateFin())
                .duree(dto.duree()) // Consider calculating duration or validating against dates
                .technicianInfo(technicianInfo)
                .panne(panne)
                .build();

        Intervention savedIntervention = interventionRepository.save(intervention);
        return toDTO(savedIntervention);
    }

    @Transactional
    @Override
    public InterventionDTO updateIntervention(UUID id, InterventionDTO dto) {
        Intervention intervention = interventionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Intervention not found with id: " + id));

        validateInterventionDates(dto.dateDebut(), dto.dateFin());

        TechnicianInfo technicianInfo = fetchTechnicianIfPresent(dto.technicianId());
        Panne panne = fetchPanneIfPresent(dto.panneId());

        // Ensure we use the correct technician ID for conflict check (either the old one if not changing, or the new one)
        UUID technicianIdToCheck = (technicianInfo != null) ? technicianInfo.getId() : null;
        // If the technician is being removed (technicianInfo is null), we don't need to check conflicts for them.
        // If the technician remains the same or changes, check conflicts for the target technician.
        if (technicianIdToCheck != null && dto.dateDebut() != null && dto.dateFin() != null) {
            checkForConflictingInterventions(technicianIdToCheck, dto.dateDebut(), dto.dateFin(), id);
        }

        // Optional: Add similar checks for Panne status and Technician availability as in create

        intervention.setDateDebut(dto.dateDebut());
        intervention.setDateFin(dto.dateFin());
        intervention.setDuree(dto.duree());
        intervention.setTechnicianInfo(technicianInfo); // Handles setting to null
        intervention.setPanne(panne); // Handles setting to null

        Intervention updatedIntervention = interventionRepository.save(intervention);
        return toDTO(updatedIntervention);
    }

    @Transactional
    @Override
    public void deleteIntervention(UUID id) {
        if (!interventionRepository.existsById(id)) {
            throw new EntityNotFoundException("Intervention not found with id: " + id);
        }
        interventionRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    @Override
    public InterventionDTO getInterventionById(UUID id) {
        Intervention intervention = interventionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Intervention not found with id: " + id));
        return toDTO(intervention);
    }

    @Transactional(readOnly = true)
    @Override
    public List<InterventionDTO> getAllInterventions() {
        return interventionRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    private void validateInterventionDates(LocalDateTime debut, LocalDateTime fin) {
        if (debut != null && fin != null && !debut.isBefore(fin)) {
            throw new IllegalArgumentException("Start date must be before end date.");
        }
        // Add other date-related checks if needed (e.g., not in the past, etc.)
    }

    private TechnicianInfo fetchTechnicianIfPresent(UUID technicianId) {
        if (technicianId == null) {
            return null;
        }
        return technicianInfoRepository.findById(technicianId)
                .orElseThrow(() -> new EntityNotFoundException("TechnicianInfo not found with id: " + technicianId));
    }

    private Panne fetchPanneIfPresent(UUID panneId) {
        if (panneId == null) {
            return null; // Intervention might not be linked to a Panne initially
            // Alternatively, throw if Panne is mandatory:
            // throw new IllegalArgumentException("Panne ID cannot be null for an intervention.");
        }
        return panneRepository.findById(panneId)
                .orElseThrow(() -> new EntityNotFoundException("Panne not found with id: " + panneId));
    }

    // Requires custom query in InterventionRepository
    private void checkForConflictingInterventions(UUID technicianInfoId, LocalDateTime debut, LocalDateTime fin, UUID interventionIdToExclude) {
        List<Intervention> conflicting = interventionRepository
                .findConflictingInterventionsForTechnician(technicianInfoId, debut, fin, interventionIdToExclude);
        if (!conflicting.isEmpty()) {
            throw new IllegalArgumentException("Technician already has a conflicting intervention scheduled during this time.");
        }
    }

    /*
    // Optional: Requires DisponibiliteRepository and logic to check against available slots
    private void checkTechnicianAvailability(UUID technicianInfoId, LocalDateTime debut, LocalDateTime fin) {
        // Query DisponibiliteRepository to find if there's an availability slot covering debut to fin
        // boolean isAvailable = ... complex query logic ...
        // if (!isAvailable) {
        //     throw new IllegalArgumentException("Technician is not available during the specified period.");
        // }
    }

    // Optional: Requires Panne model to have a status field/enum
    private void validatePanneStatus(Panne panne) {
        if (panne != null && panne.getStatus() == PanneStatus.RESOLVED) { // Assuming PanneStatus enum/field exists
             throw new IllegalArgumentException("Cannot create or update intervention for an already resolved panne.");
        }
        // Add other status checks as needed
    }
    */

    private InterventionDTO toDTO(Intervention intervention) {
        if (intervention == null) {
            return null;
        }
        UUID technicianId = intervention.getTechnicianInfo() != null ? intervention.getTechnicianInfo().getId() : null;
        UUID panneId = intervention.getPanne() != null ? intervention.getPanne().getId() : null;

        return new InterventionDTO(
                intervention.getId(),
                intervention.getDateDebut(),
                intervention.getDateFin(),
                intervention.getDuree(),
                technicianId,
                panneId
        );
    }
}