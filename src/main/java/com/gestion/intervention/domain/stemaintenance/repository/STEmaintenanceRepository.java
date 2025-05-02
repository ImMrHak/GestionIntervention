package com.gestion.intervention.domain.stemaintenance.repository;

import com.gestion.intervention.domain.stemaintenance.model.STEmaintenance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface STEmaintenanceRepository extends JpaRepository<STEmaintenance, UUID> {
    Optional<STEmaintenance> findByNom(String nom);
}
