package com.gestion.intervention.application.panne.service;

import com.gestion.intervention.application.panne.record.PanneDTO;
import com.gestion.intervention.domain.intervention.repository.InterventionRepository; // Added dependency
import com.gestion.intervention.domain.machine.model.Machine;
import com.gestion.intervention.domain.machine.repository.MachineRepository;
import com.gestion.intervention.domain.panne.enumeration.PanneStatus;
import com.gestion.intervention.domain.panne.model.Panne;
// Assuming Panne has a status enum or field, e.g., PanneStatus.ACTIVE
// import com.gestion.intervention.domain.panne.model.PanneStatus;
import com.gestion.intervention.domain.panne.repository.PanneRepository;
import com.gestion.intervention.domain.person.model.Person;
import com.gestion.intervention.domain.person.repository.PersonRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PanneServiceImpl implements PanneService {

    private final PanneRepository panneRepository;
    private final MachineRepository machineRepository;
    private final PersonRepository personRepository;
    private final InterventionRepository interventionRepository; // Added dependency

    @Transactional
    @Override
    public PanneDTO createPanne(PanneDTO dto) {
        validatePanneInput(dto);

        Machine machine = fetchMachineAndValidateState(dto.machineId());
        Person reporter = fetchReporter(dto.reporterId());

        // Check for duplicate active pannes of the same type for the same machine
        checkForDuplicateActivePanne(dto.typePanne(), dto.machineId(), null);

        Panne panne = Panne.builder()
                .typePanne(dto.typePanne())
                .machine(machine)
                .reporter(reporter)
                // Set initial status if applicable, e.g., ACTIVE
                // .status(PanneStatus.ACTIVE)
                .build();
        Panne savedPanne = panneRepository.save(panne);
        return toDto(savedPanne);
    }

    @Transactional(readOnly = true)
    @Override
    public PanneDTO getPanneById(UUID id) {
        Panne panne = panneRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Panne not found with id: " + id));
        return toDto(panne);
    }

    @Transactional(readOnly = true)
    @Override
    public List<PanneDTO> getAllPannes() {
        return panneRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public PanneDTO updatePanne(UUID id, PanneDTO dto) {
        Panne panne = panneRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Panne not found with id: " + id));

        validatePanneInput(dto);

        Machine machine = fetchMachineAndValidateState(dto.machineId());
        Person reporter = fetchReporter(dto.reporterId());

        // Check for duplicates only if type or machine changes and the panne is considered active
        // This check might need refinement based on specific business rules for updates
        boolean needsDuplicateCheck = !panne.getTypePanne().equals(dto.typePanne()) ||
                !panne.getMachine().getId().equals(dto.machineId());
        // && panne.getStatus() == PanneStatus.ACTIVE; // Add status check if applicable

        if (needsDuplicateCheck) {
            checkForDuplicateActivePanne(dto.typePanne(), dto.machineId(), id);
        }

        // Validate status transitions if Panne has a status field
        // validateStatusTransition(panne, dto.status()); // Assuming status is part of DTO

        panne.setTypePanne(dto.typePanne());
        panne.setMachine(machine);
        panne.setReporter(reporter);
        // panne.setStatus(dto.status()); // Update status if applicable

        Panne updatedPanne = panneRepository.save(panne);
        return toDto(updatedPanne);
    }

    @Transactional
    @Override
    public void deletePanne(UUID id) {
        if (!panneRepository.existsById(id)) {
            throw new EntityNotFoundException("Panne not found with id: " + id);
        }

        // Check if there are associated Interventions
        checkPanneAssociations(id);

        panneRepository.deleteById(id);
    }

    @Transactional
    @Override
    public Integer getTotalPannesByStatus(PanneStatus status, UUID userId) {
        return panneRepository.countAllByStatusAndReporter_Id(status, userId);
    }

    @Override
    public Integer getTotalPannesByReporterId(UUID reporterId) {
        return panneRepository.countAllByReporter_Id(reporterId);
    }

    private void validatePanneInput(PanneDTO dto) {
        if (dto.machineId() == null) {
            throw new IllegalArgumentException("Machine ID cannot be null when reporting a panne.");
        }
        if (dto.typePanne() == null || dto.typePanne().trim().isEmpty()) {
            throw new IllegalArgumentException("Panne type (typePanne) cannot be null or empty.");
        }
        // Reporter might be optional depending on requirements
        // if (dto.reporterId() == null) {
        //     throw new IllegalArgumentException("Reporter ID cannot be null.");
        // }
    }

    private Machine fetchMachineAndValidateState(UUID machineId) {
        Machine machine = machineRepository.findById(machineId)
                .orElseThrow(() -> new EntityNotFoundException("Machine not found with id: " + machineId));

        // Example state check: prevent reporting panne if machine is OUT_OF_SERVICE
        if ("OUT_OF_SERVICE".equalsIgnoreCase(machine.getEtat())) {
            throw new IllegalStateException("Cannot report a panne for a machine that is already OUT_OF_SERVICE.");
        }
        // Add other relevant state checks (e.g., maybe cannot report if status is 'NEW' or 'UNDER_MAINTENANCE')

        return machine;
    }

    private Person fetchReporter(UUID reporterId) {
        if (reporterId == null) {
            return null; // Assuming reporter is optional
        }
        return personRepository.findById(reporterId)
                .orElseThrow(() -> new EntityNotFoundException("Reporter (Person) not found with id: " + reporterId));
    }

    // Requires custom query in PanneRepository
    private void checkForDuplicateActivePanne(String typePanne, UUID machineId, UUID panneIdToExclude) {
        // This assumes Panne has a status and you only care about duplicates if they are "active"
        // Adjust the repository method call based on your actual implementation (e.g., findActiveByTypeAndMachine)
        Optional<Panne> existingActivePanne = panneRepository
                .findActivePanneByTypeAndMachine(typePanne, machineId); // Modify repository method as needed

        if (existingActivePanne.isPresent() && (panneIdToExclude == null || !existingActivePanne.get().getId().equals(panneIdToExclude))) {
            throw new IllegalArgumentException("An active panne of type '" + typePanne + "' already exists for machine with id: " + machineId);
        }
    }

    private void checkPanneAssociations(UUID panneId) {
        // Check if any Intervention is linked to this Panne
        // Assumes InterventionRepository has existsByPanneId method
        boolean hasInterventions = interventionRepository.existsByPanneId(panneId);
        if (hasInterventions) {
            // You might want to only prevent deletion if interventions are *active* or *not completed*
            // boolean hasActiveInterventions = interventionRepository.existsActiveByPanneId(panneId);
            throw new IllegalStateException("Cannot delete Panne with id: " + panneId + " because it is associated with one or more Interventions.");
        }
    }

    /*
    // Example validation for status transition if PanneDTO and Panne have a status field/enum
    private void validateStatusTransition(Panne existingPanne, PanneStatus newStatus) {
        if (existingPanne.getStatus() == PanneStatus.RESOLVED && newStatus != PanneStatus.RESOLVED) {
            throw new IllegalArgumentException("Cannot change status of a resolved panne.");
        }
        // Add other transition rules (e.g., RESOLVED -> ACTIVE might be disallowed)
    }
    */

    private PanneDTO toDto(Panne panne) {
        if (panne == null) {
            return null;
        }
        UUID machineId = panne.getMachine() != null ? panne.getMachine().getId() : null;
        UUID reporterId = panne.getReporter() != null ? panne.getReporter().getId() : null;

        return new PanneDTO(
                panne.getId(),
                panne.getTypePanne(),
                machineId,
                reporterId
                // Include status if applicable: panne.getStatus()
        );
    }
}