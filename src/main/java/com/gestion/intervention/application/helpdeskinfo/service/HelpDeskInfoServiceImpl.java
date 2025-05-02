package com.gestion.intervention.application.helpdeskinfo.service;

import com.gestion.intervention.application.helpdeskinfo.record.HelpDeskInfoDTO;
import com.gestion.intervention.domain.helpdeskinfo.model.HelpDeskInfo;
import com.gestion.intervention.domain.helpdeskinfo.repository.HelpDeskInfoRepository;
import com.gestion.intervention.domain.person.model.Person;
import com.gestion.intervention.domain.person.repository.PersonRepository;
import com.gestion.intervention.domain.stemaintenance.model.STEmaintenance;
import com.gestion.intervention.domain.stemaintenance.repository.STEmaintenanceRepository;
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
public class HelpDeskInfoServiceImpl implements HelpDeskInfoService {
    private final HelpDeskInfoRepository helpDeskInfoRepository;
    private final PersonRepository personRepository;
    private final STEmaintenanceRepository steMaintenanceRepository;

    @Transactional
    @Override
    public HelpDeskInfoDTO createHelpDeskInfo(HelpDeskInfoDTO dto) {
        validateHelpDeskLinkage(dto.personId(), dto.steId());

        Person person = fetchPersonIfPresent(dto.personId());
        STEmaintenance ste = fetchSteIfPresent(dto.steId());

        // Optional: Check if a HelpDeskInfo already exists for this person or STE
        checkDuplicateHelpDeskInfo(dto.personId(), dto.steId());

        HelpDeskInfo helpDeskInfo = HelpDeskInfo.builder()
                .person(person)
                .ste(ste)
                .build();
        HelpDeskInfo savedHelpDeskInfo = helpDeskInfoRepository.save(helpDeskInfo);
        return toDTO(savedHelpDeskInfo);
    }

    @Transactional
    @Override
    public HelpDeskInfoDTO updateHelpDeskInfo(UUID id, HelpDeskInfoDTO dto) {
        HelpDeskInfo helpDeskInfo = helpDeskInfoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("HelpDeskInfo not found with id: " + id));

        validateHelpDeskLinkage(dto.personId(), dto.steId());

        // Optional: Check for duplicates before updating links, excluding the current entity
        checkDuplicateHelpDeskInfoForUpdate(dto.personId(), dto.steId(), id);

        Person person = fetchPersonIfPresent(dto.personId());
        STEmaintenance ste = fetchSteIfPresent(dto.steId());

        helpDeskInfo.setPerson(person); // Sets to null if personId is null
        helpDeskInfo.setSte(ste);       // Sets to null if steId is null

        HelpDeskInfo updatedHelpDeskInfo = helpDeskInfoRepository.save(helpDeskInfo);
        return toDTO(updatedHelpDeskInfo);
    }

    @Transactional
    @Override
    public void deleteHelpDeskInfo(UUID id) {
        if (!helpDeskInfoRepository.existsById(id)) {
            throw new EntityNotFoundException("HelpDeskInfo not found with id: " + id);
        }
        helpDeskInfoRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    @Override
    public HelpDeskInfoDTO getHelpDeskInfoById(UUID id) {
        HelpDeskInfo helpDeskInfo = helpDeskInfoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("HelpDeskInfo not found with id: " + id));
        return toDTO(helpDeskInfo);
    }

    @Transactional(readOnly = true)
    @Override
    public List<HelpDeskInfoDTO> getAllHelpDeskInfos() {
        return helpDeskInfoRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    private void validateHelpDeskLinkage(UUID personId, UUID steId) {
        if (personId != null && steId != null) {
            throw new IllegalArgumentException("HelpDeskInfo cannot be linked to both a Person and an STEmaintenance simultaneously.");
        }
        if (personId == null && steId == null) {
            throw new IllegalArgumentException("HelpDeskInfo must be linked to either a Person or an STEmaintenance.");
        }
    }

    private Person fetchPersonIfPresent(UUID personId) {
        if (personId == null) {
            return null;
        }
        return personRepository.findById(personId)
                .orElseThrow(() -> new EntityNotFoundException("Person not found with id: " + personId));
    }

    private STEmaintenance fetchSteIfPresent(UUID steId) {
        if (steId == null) {
            return null;
        }
        return steMaintenanceRepository.findById(steId)
                .orElseThrow(() -> new EntityNotFoundException("STEmaintenance not found with id: " + steId));
    }

    // Optional duplicate checks (require methods in HelpDeskInfoRepository)
    private void checkDuplicateHelpDeskInfo(UUID personId, UUID steId) {
        if (personId != null && helpDeskInfoRepository.existsByPersonId(personId)) {
            throw new IllegalArgumentException("A HelpDeskInfo entry already exists for Person with id: " + personId);
        }
        if (steId != null && helpDeskInfoRepository.existsBySteId(steId)) {
            throw new IllegalArgumentException("A HelpDeskInfo entry already exists for STEmaintenance with id: " + steId);
        }
    }

    private void checkDuplicateHelpDeskInfoForUpdate(UUID personId, UUID steId, UUID currentHelpDeskId) {
        if (personId != null) {
            Optional<HelpDeskInfo> existing = helpDeskInfoRepository.findByPersonId(personId);
            if (existing.isPresent() && !existing.get().getId().equals(currentHelpDeskId)) {
                throw new IllegalArgumentException("Another HelpDeskInfo entry (id: " + existing.get().getId() + ") already exists for Person with id: " + personId);
            }
        }
        if (steId != null) {
            Optional<HelpDeskInfo> existing = helpDeskInfoRepository.findBySteId(steId);
            if (existing.isPresent() && !existing.get().getId().equals(currentHelpDeskId)) {
                throw new IllegalArgumentException("Another HelpDeskInfo entry (id: " + existing.get().getId() + ") already exists for STEmaintenance with id: " + steId);
            }
        }
    }

    private HelpDeskInfoDTO toDTO(HelpDeskInfo helpDeskInfo) {
        if (helpDeskInfo == null) {
            return null;
        }
        UUID personId = helpDeskInfo.getPerson() != null ? helpDeskInfo.getPerson().getId() : null;
        UUID steId = helpDeskInfo.getSte() != null ? helpDeskInfo.getSte().getId() : null;

        return new HelpDeskInfoDTO(
                helpDeskInfo.getId(),
                personId,
                steId
        );
    }
}