package com.gestion.intervention.application.stemaintenance.service;

import com.gestion.intervention.application.stemaintenance.record.STEmaintenanceDTO;
// Import necessary repositories for association checks, e.g., HelpDeskInfo
// import com.gestion.intervention.domain.helpdeskinfo.repository.HelpDeskInfoRepository;
import com.gestion.intervention.domain.stemaintenance.model.STEmaintenance;
import com.gestion.intervention.domain.stemaintenance.repository.STEmaintenanceRepository;
import jakarta.persistence.EntityNotFoundException; // Import correct exception
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional; // Added import
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class STEmaintenanceServiceImpl implements STEmaintenanceService {

    private final STEmaintenanceRepository stEmaintenanceRepository;
    // Inject repositories needed for association checks
    // private final HelpDeskInfoRepository helpDeskInfoRepository;

    @Transactional
    @Override
    public STEmaintenanceDTO createSTEmaintenance(STEmaintenanceDTO dto) {
        validateSteName(dto.nom());
        checkDuplicateSteName(dto.nom(), null); // Check uniqueness

        // Use builder pattern for consistency if available, or constructor
        STEmaintenance stEmaintenance = STEmaintenance.builder()
                .nom(dto.nom())
                .build();
        // If no builder:
        // STEmaintenance stEmaintenance = new STEmaintenance();
        // stEmaintenance.setNom(dto.nom());

        STEmaintenance savedSte = stEmaintenanceRepository.save(stEmaintenance);
        return toDTO(savedSte);
    }

    @Transactional
    @Override
    public STEmaintenanceDTO updateSTEmaintenance(UUID id, STEmaintenanceDTO dto) {
        STEmaintenance stEmaintenance = stEmaintenanceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("STEmaintenance not found with id: " + id)); // Use specific exception

        validateSteName(dto.nom());

        // Check uniqueness only if the name is actually changing
        if (!stEmaintenance.getNom().equalsIgnoreCase(dto.nom())) {
            checkDuplicateSteName(dto.nom(), id);
        }

        stEmaintenance.setNom(dto.nom());
        STEmaintenance updatedSte = stEmaintenanceRepository.save(stEmaintenance);
        return toDTO(updatedSte);
    }

    @Transactional
    @Override
    public void deleteSTEmaintenance(UUID id) {
        // Explicitly check existence before deleting
        if (!stEmaintenanceRepository.existsById(id)) {
            throw new EntityNotFoundException("STEmaintenance not found with id: " + id);
        }

        // Optional: Check for associations before deleting
        // checkSteAssociations(id);

        stEmaintenanceRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true) // Add readOnly = true for getter methods
    public STEmaintenanceDTO getSTEmaintenanceById(UUID id) {
        return stEmaintenanceRepository.findById(id)
                .map(this::toDTO) // Use map for cleaner conversion
                .orElseThrow(() -> new EntityNotFoundException("STEmaintenance not found with id: " + id)); // Use specific exception
    }

    @Override
    @Transactional(readOnly = true) // Add readOnly = true for getter methods
    public List<STEmaintenanceDTO> getAllSTEmaintenances() {
        return stEmaintenanceRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    private void validateSteName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("STEmaintenance name (nom) cannot be null or empty.");
        }
        // Add other validation rules for the name if necessary (length, characters, etc.)
    }

    // Requires custom query in STEmaintenanceRepository
    private void checkDuplicateSteName(String name, UUID steIdToExclude) {
        Optional<STEmaintenance> existingSte = stEmaintenanceRepository.findByNom(name); // Case-insensitive find might be better

        if (existingSte.isPresent() && (steIdToExclude == null || !existingSte.get().getId().equals(steIdToExclude))) {
            throw new IllegalArgumentException("An STEmaintenance with the name '" + name + "' already exists.");
        }
    }

    /*
    // Optional: Check associations before deletion
    private void checkSteAssociations(UUID steId) {
        // Example: Check if linked to HelpDeskInfo
        if (helpDeskInfoRepository.existsBySteId(steId)) { // Requires HelpDeskInfoRepository and method
             throw new IllegalStateException("Cannot delete STEmaintenance with id: " + steId + " as it is associated with HelpDeskInfo records.");
        }
        // Add checks for other potential associations
    }
    */

    private STEmaintenanceDTO toDTO(STEmaintenance stEmaintenance) {
        if (stEmaintenance == null) {
            return null;
        }
        return new STEmaintenanceDTO(
                stEmaintenance.getId(),
                stEmaintenance.getNom()
        );
    }
}