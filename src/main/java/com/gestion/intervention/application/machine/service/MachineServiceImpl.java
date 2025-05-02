package com.gestion.intervention.application.machine.service;

import com.gestion.intervention.application.machine.record.MachineDTO;
import com.gestion.intervention.domain.machine.model.Machine;
import com.gestion.intervention.domain.machine.repository.MachineRepository;
import com.gestion.intervention.domain.panne.repository.PanneRepository; // Added import (assuming Panne refers to Machine)
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays; // Added for etat validation
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MachineServiceImpl implements MachineService {

    private final MachineRepository machineRepository;
    private final PanneRepository panneRepository; // Added dependency (assuming Panne links to Machine)

    // Optional: Define allowed states if not using an enum
    private static final List<String> ALLOWED_ETATS = Arrays.asList("OPERATIONAL", "MAINTENANCE", "OUT_OF_SERVICE", "NEW");

    @Override
    @Transactional
    public MachineDTO createMachine(MachineDTO dto) {
        validateMachineEtat(dto.etat());
        // Optional: Add check for unique constraints if applicable (e.g., based on type or a serial number if added)
        // checkDuplicateMachine(dto.type()); // Requires a repository method

        Machine entity = Machine.builder()
                .type(dto.type())
                .etat(dto.etat())
                .build();
        Machine savedEntity = machineRepository.save(entity);
        return toDto(savedEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public MachineDTO getMachineById(UUID id) {
        return machineRepository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new EntityNotFoundException("Machine not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<MachineDTO> getAllMachines() {
        return machineRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public MachineDTO updateMachine(UUID id, MachineDTO dto) {
        Machine entity = machineRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Machine not found with id: " + id));

        validateMachineEtat(dto.etat());
        // Optional: Add check for unique constraints if applicable when changing identifying fields

        // Optional: Add checks based on state transitions (e.g., cannot set to OPERATIONAL if has open Panne)
        // validateStateTransition(entity, dto.etat());

        entity.setType(dto.type());
        entity.setEtat(dto.etat());

        Machine updatedEntity = machineRepository.save(entity);
        return toDto(updatedEntity);
    }

    @Override
    @Transactional
    public void deleteMachine(UUID id) {
        if (!machineRepository.existsById(id)) {
            throw new EntityNotFoundException("Machine not found with id: " + id);
        }
        // Check if the machine is associated with any Panne records
        checkMachineAssociations(id);

        machineRepository.deleteById(id);
    }

    private void validateMachineEtat(String etat) {
        // This check can be basic or complex. Using a predefined list as an example.
        // Ideally, 'etat' would be an Enum, making this check unnecessary or handled by Enum validation.
        if (etat != null && !ALLOWED_ETATS.contains(etat.toUpperCase())) {
            throw new IllegalArgumentException("Invalid machine state (etat) provided: " + etat + ". Allowed states are: " + ALLOWED_ETATS);
        }
        // Add null check if 'etat' is mandatory
        if (etat == null || etat.trim().isEmpty()) {
            throw new IllegalArgumentException("Machine state (etat) cannot be null or empty.");
        }
    }

    private void checkMachineAssociations(UUID machineId) {
        // Assuming PanneRepository has a method to count or check existence by machineId
        // Adjust 'existsByMachineId' to your actual repository method name
        boolean hasPannes = panneRepository.existsByMachineId(machineId);
        if (hasPannes) {
            // Consider only checking for *active* pannes if applicable
            // boolean hasActivePannes = panneRepository.existsActiveByMachineId(machineId);
            throw new IllegalStateException("Cannot delete Machine with id: " + machineId + " because it is associated with one or more Pannes.");
        }
        // Add similar checks for other associations (e.g., active Interventions linked via Panne) if needed
    }

    /*
    // Example of a unique check (if type were unique, which is unlikely for machines)
    private void checkDuplicateMachine(String type) {
        if (type != null && machineRepository.existsByType(type)) {
             throw new IllegalArgumentException("A machine with type '" + type + "' already exists.");
        }
    }

    // Example of state transition validation
    private void validateStateTransition(Machine existingMachine, String newEtat) {
        if ("OPERATIONAL".equalsIgnoreCase(newEtat)) {
            boolean hasActivePannes = panneRepository.existsActiveByMachineId(existingMachine.getId());
             if (hasActivePannes) {
                 throw new IllegalStateException("Cannot set machine state to OPERATIONAL while it has active pannes.");
             }
        }
        // Add other transition rules
    }
    */


    private MachineDTO toDto(Machine entity) {
        if (entity == null) {
            return null;
        }
        return new MachineDTO(
                entity.getId(),
                entity.getType(),
                entity.getEtat()
        );
    }
}