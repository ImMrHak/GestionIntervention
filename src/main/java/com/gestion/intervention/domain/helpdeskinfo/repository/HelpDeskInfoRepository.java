package com.gestion.intervention.domain.helpdeskinfo.repository;

import com.gestion.intervention.domain.helpdeskinfo.model.HelpDeskInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface HelpDeskInfoRepository extends JpaRepository<HelpDeskInfo, UUID> {
    boolean existsByPersonId(UUID personId);
    boolean existsBySteId(UUID steId);

    Optional<HelpDeskInfo> findByPersonId(UUID personId);
    Optional<HelpDeskInfo> findBySteId(UUID steId);
}
