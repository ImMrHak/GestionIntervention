package com.gestion.intervention.domain.technicianinfo.repository;

import com.gestion.intervention.domain.technicianinfo.model.TechnicianInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TechnicianInfoRepository extends JpaRepository<TechnicianInfo, UUID> {
    boolean existsByPersonId(UUID personId);
    Optional<TechnicianInfo> findByPersonId(UUID personId);
}
