package com.gestion.intervention.domain.panne.repository;

import com.gestion.intervention.domain.panne.model.Panne;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PanneRepository extends JpaRepository<Panne, UUID> {
    boolean existsByMachineId(UUID machineId);
    @Query("SELECT p FROM Panne p WHERE p.typePanne = :typePanne AND p.machine.id = :machineId")
    Optional<Panne> findActivePanneByTypeAndMachine(@Param("typePanne") String typePanne, @Param("machineId") UUID machineId);
    boolean existsActiveByReporterId(UUID reporterId);
}
