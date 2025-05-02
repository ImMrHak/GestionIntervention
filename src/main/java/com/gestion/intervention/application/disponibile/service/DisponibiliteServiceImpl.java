package com.gestion.intervention.application.disponibile.service;

import com.gestion.intervention.application.disponibile.record.DisponibiliteDTO;
import com.gestion.intervention.domain.disponibile.model.Disponibilite;
import com.gestion.intervention.domain.disponibile.repository.DisponibiliteRepository;
import com.gestion.intervention.domain.technicianinfo.model.TechnicianInfo;
import com.gestion.intervention.domain.technicianinfo.repository.TechnicianInfoRepository; // Added import
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Added import

import java.time.LocalDateTime; // Added import
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DisponibiliteServiceImpl implements DisponibiliteService {

    private final DisponibiliteRepository disponibiliteRepository;
    private final TechnicianInfoRepository technicianInfoRepository; // Added field

    @Override
    @Transactional
    public DisponibiliteDTO createDisponibilite(DisponibiliteDTO dto) {
        validateDisponibiliteDates(dto.debut(), dto.fin());

        if (!technicianInfoRepository.existsById(dto.technicianInfoId())) {
            throw new EntityNotFoundException("TechnicianInfo not found with id: " + dto.technicianInfoId());
        }

        checkForOverlappingDisponibilite(dto.technicianInfoId(), dto.debut(), dto.fin(), null);

        Disponibilite disponibilite = Disponibilite.builder()
                .technicianInfo(TechnicianInfo.builder().id(dto.technicianInfoId()).build())
                .etat(dto.etat())
                .debut(dto.debut())
                .fin(dto.fin())
                .build();

        Disponibilite savedDisponibilite = disponibiliteRepository.save(disponibilite);
        return toDto(savedDisponibilite);
    }

    @Override
    @Transactional(readOnly = true)
    public DisponibiliteDTO getDisponibiliteById(UUID id) {
        return disponibiliteRepository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new EntityNotFoundException("Disponibilite not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<DisponibiliteDTO> getAllDisponibilites() {
        return disponibiliteRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public DisponibiliteDTO updateDisponibilite(UUID id, DisponibiliteDTO dto) {
        Disponibilite disponibilite = disponibiliteRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Disponibilite not found with id: " + id));

        validateDisponibiliteDates(dto.debut(), dto.fin());

        checkForOverlappingDisponibilite(disponibilite.getTechnicianInfo().getId(), dto.debut(), dto.fin(), id);

        disponibilite.setEtat(dto.etat());
        disponibilite.setDebut(dto.debut());
        disponibilite.setFin(dto.fin());

        Disponibilite updatedDisponibilite = disponibiliteRepository.save(disponibilite);
        return toDto(updatedDisponibilite);
    }

    @Override
    @Transactional
    public void deleteDisponibilite(UUID id) {
        if (!disponibiliteRepository.existsById(id)) {
            throw new EntityNotFoundException("Disponibilite not found with id: " + id);
        }
        disponibiliteRepository.deleteById(id);
    }

    private void validateDisponibiliteDates(LocalDateTime debut, LocalDateTime fin) {
        if (debut == null || fin == null) {
            throw new IllegalArgumentException("Start date and end date cannot be null.");
        }
        if (!debut.isBefore(fin)) {
            throw new IllegalArgumentException("Start date must be before end date.");
        }
    }

    private void checkForOverlappingDisponibilite(UUID technicianInfoId, LocalDateTime debut, LocalDateTime fin, UUID idToExclude) {
        List<Disponibilite> overlapping = disponibiliteRepository
                .findOverlapping(technicianInfoId, debut, fin, idToExclude);

        if (!overlapping.isEmpty()) {
            throw new IllegalArgumentException("The provided availability period overlaps with an existing one for this technician.");
        }
    }


    private DisponibiliteDTO toDto(Disponibilite disponibilite) {
        UUID techId = (disponibilite.getTechnicianInfo() != null) ? disponibilite.getTechnicianInfo().getId() : null;
        return new DisponibiliteDTO(
                disponibilite.getId(),
                techId,
                disponibilite.getEtat(),
                disponibilite.getDebut(),
                disponibilite.getFin()
        );
    }
}