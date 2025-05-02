package com.gestion.intervention.application.technicianinfo.service;

import com.gestion.intervention.application.technicianinfo.record.TechnicianInfoDTO;
import com.gestion.intervention.domain.disponibile.repository.DisponibiliteRepository; // Added import
import com.gestion.intervention.domain.intervention.repository.InterventionRepository; // Added import
import com.gestion.intervention.domain.person.model.Person;
import com.gestion.intervention.domain.person.repository.PersonRepository; // Added import
import com.gestion.intervention.domain.stemaintenance.model.STEmaintenance; // Added import (if idSte links here)
import com.gestion.intervention.domain.stemaintenance.repository.STEmaintenanceRepository; // Added import (if idSte links here)
import com.gestion.intervention.domain.technicianinfo.model.TechnicianInfo;
import com.gestion.intervention.domain.technicianinfo.repository.TechnicianInfoRepository;
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
public class TechnicianInfoServiceImpl implements TechnicianInfoService {

    private final TechnicianInfoRepository technicianInfoRepository;
    private final PersonRepository personRepository; // Added dependency
    // Assuming idSte refers to STEmaintenance ID. If not, adjust/remove STEmaintenanceRepository.
    private final STEmaintenanceRepository steMaintenanceRepository; // Added dependency
    private final InterventionRepository interventionRepository; // Added dependency
    private final DisponibiliteRepository disponibiliteRepository; // Added dependency

    @Override
    @Transactional
    public TechnicianInfoDTO createTechnicianInfo(TechnicianInfoDTO dto) {
        validateTechnicianInfoInput(dto);

        Person person = fetchPerson(dto.personId());
        // Optional: Fetch STEmaintenance if idSte is a foreign key
        STEmaintenance ste = fetchSteMaintenanceIfApplicable(dto.id()); // Use if idSte links to STEmaintenance

        // Check if TechnicianInfo already exists for this Person
        checkDuplicateTechnicianInfoByPerson(dto.personId(), null);

        TechnicianInfo entity = TechnicianInfo.builder()
                // .id(dto.id()) // ID is usually auto-generated
                .person(person)
                // Only set STE if idSte is a FK to STEmaintenance. Otherwise, store the UUID directly.
                // .ste(ste) // If idSte is FK to STEmaintenance object
                .idSte(dto.idSte()) // If idSte is just a UUID field
                .specialite(dto.specialite())
                .nbrPanne(dto.nbrPanne())
                .nbrPanneRegle(dto.nbrPanneRegle())
                .build();

        TechnicianInfo savedEntity = technicianInfoRepository.save(entity);
        return toDto(savedEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public TechnicianInfoDTO getTechnicianInfoById(UUID id) {
        return technicianInfoRepository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new EntityNotFoundException("TechnicianInfo not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TechnicianInfoDTO> getAllTechnicianInfos() {
        return technicianInfoRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public TechnicianInfoDTO updateTechnicianInfo(UUID id, TechnicianInfoDTO dto) {
        TechnicianInfo entity = technicianInfoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("TechnicianInfo not found with id: " + id));

        // Basic validation for potentially updated fields
        validateTechnicianCounters(dto.nbrPanne(), dto.nbrPanneRegle());
        if (dto.specialite() == null || dto.specialite().trim().isEmpty()) {
            throw new IllegalArgumentException("Specialite cannot be empty.");
        }

        // --- IMPORTANT ---
        // Decide if personId or idSte can be changed. Usually, they shouldn't be easily updatable.
        // If they CAN be changed, add validation and uniqueness checks like in create:
        /*
        if (!entity.getPerson().getId().equals(dto.personId())) {
            Person newPerson = fetchPerson(dto.personId());
            checkDuplicateTechnicianInfoByPerson(dto.personId(), id);
            entity.setPerson(newPerson);
        }
        if (!entity.getIdSte().equals(dto.idSte())) {
             // Assuming idSte is just UUID, not FK object
             // fetchSteMaintenanceIfApplicable(dto.idSte()); // Validate if needed
             entity.setIdSte(dto.idSte());
        }
        */
        // For this example, assume Person and STE cannot be changed via this update method.

        // Update only the allowed fields
        entity.setSpecialite(dto.specialite());
        entity.setNbrPanne(dto.nbrPanne());
        entity.setNbrPanneRegle(dto.nbrPanneRegle());

        TechnicianInfo updatedEntity = technicianInfoRepository.save(entity);
        return toDto(updatedEntity);
    }

    @Override
    @Transactional
    public void deleteTechnicianInfo(UUID id) {
        if (!technicianInfoRepository.existsById(id)) {
            throw new EntityNotFoundException("TechnicianInfo not found with id: " + id);
        }

        // Check for associations before deleting
        checkTechnicianAssociations(id);

        technicianInfoRepository.deleteById(id);
    }

    private void validateTechnicianInfoInput(TechnicianInfoDTO dto) {
        if (dto.personId() == null) {
            throw new IllegalArgumentException("Person ID cannot be null for TechnicianInfo.");
        }
        if (dto.specialite() == null || dto.specialite().trim().isEmpty()) {
            throw new IllegalArgumentException("Specialite cannot be null or empty.");
        }
        // Assuming idSte is optional or validated elsewhere if it's just a UUID
        // if (dto.idSte() == null) {
        //     throw new IllegalArgumentException("STE ID cannot be null.");
        // }
        validateTechnicianCounters(dto.nbrPanne(), dto.nbrPanneRegle());
    }

    private void validateTechnicianCounters(Integer nbrPanne, Integer nbrPanneRegle) {
        // Default to 0 if null, or throw if they are mandatory
        int pannes = (nbrPanne == null) ? 0 : nbrPanne;
        int pannesReglees = (nbrPanneRegle == null) ? 0 : nbrPanneRegle;

        if (pannes < 0 || pannesReglees < 0) {
            throw new IllegalArgumentException("Number of pannes and pannes reglees cannot be negative.");
        }
        if (pannesReglees > pannes) {
            throw new IllegalArgumentException("Number of pannes reglees cannot exceed the total number of pannes.");
        }
    }

    private Person fetchPerson(UUID personId) {
        if (personId == null) throw new IllegalArgumentException("Person ID cannot be null.");
        return personRepository.findById(personId)
                .orElseThrow(() -> new EntityNotFoundException("Person not found with id: " + personId));
    }

    // Use this only if idSte is a foreign key to the STEmaintenance table
    private STEmaintenance fetchSteMaintenanceIfApplicable(UUID Id) {
        if (Id == null) {
            return null; // Or throw if mandatory
        }
        return steMaintenanceRepository.findById(Id)
                .orElseThrow(() -> new EntityNotFoundException("STEmaintenance not found with id: " + Id));
    }

    // Requires custom query in TechnicianInfoRepository
    private void checkDuplicateTechnicianInfoByPerson(UUID personId, UUID techInfoIdToExclude) {
        Optional<TechnicianInfo> existingInfo = technicianInfoRepository.findByPersonId(personId);

        if (existingInfo.isPresent() && (techInfoIdToExclude == null || !existingInfo.get().getId().equals(techInfoIdToExclude))) {
            throw new IllegalArgumentException("TechnicianInfo already exists for Person with id: " + personId);
        }
    }

    private void checkTechnicianAssociations(UUID technicianInfoId) {
        // Check for active Interventions assigned to this technician
        // Assumes InterventionRepository has a method like existsActiveByTechnicianInfoId
        if (interventionRepository.existsActiveByTechnicianInfoId(technicianInfoId)) {
            throw new IllegalStateException("Cannot delete TechnicianInfo with id: " + technicianInfoId + " as they have active interventions assigned.");
        }

        // Check for existing Disponibilite records for this technician
        // Assumes DisponibiliteRepository has a method like existsByTechnicianInfoId
        if (disponibiliteRepository.existsByTechnicianInfoId(technicianInfoId)) {
            // Decide if this should prevent deletion. Maybe only future availabilities matter?
            // Or maybe delete availabilities along with the technician? Requires careful thought.
            // For now, prevent deletion if any exist.
            throw new IllegalStateException("Cannot delete TechnicianInfo with id: " + technicianInfoId + " as they have existing availability records.");
        }
    }

    private TechnicianInfoDTO toDto(TechnicianInfo entity) {
        if (entity == null) {
            return null;
        }
        // Ensure person is not null before accessing ID (FK constraint should prevent this)
        UUID personId = (entity.getPerson() != null) ? entity.getPerson().getId() : null;
        if (personId == null) {
            // Log warning or handle error - data inconsistency
            System.err.println("Warning: TechnicianInfo with ID " + entity.getId() + " has a null Person association.");
        }

        return new TechnicianInfoDTO(
                entity.getId(),
                personId, // Use safely retrieved ID
                entity.getIdSte(), // Assuming idSte is stored directly
                // If idSte refers to STEmaintenance object: entity.getSte() != null ? entity.getSte().getId() : null,
                entity.getSpecialite(),
                entity.getNbrPanne(),
                entity.getNbrPanneRegle()
        );
    }
}