package com.gestion.intervention.domain.intervention.repository;

import com.gestion.intervention.domain.intervention.model.Intervention;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface InterventionRepository extends JpaRepository<Intervention, UUID> {
    @Query("SELECT i FROM Intervention i " +
            "WHERE i.technicianInfo.id = :technicianInfoId " +
            "AND i.dateDebut < :newFin " +
            "AND i.dateFin > :newDebut " +
            "AND (:interventionIdToExclude IS NULL OR i.id <> :interventionIdToExclude)")
    List<Intervention> findConflictingInterventionsForTechnician(
            @Param("technicianInfoId") UUID technicianInfoId,
            @Param("newDebut") LocalDateTime newDebut,
            @Param("newFin") LocalDateTime newFin,
            @Param("interventionIdToExclude") UUID interventionIdToExclude);
    boolean existsByPanneId(UUID panneId);

    @Query("SELECT CASE WHEN COUNT(i) > 0 THEN TRUE ELSE FALSE END FROM Intervention i " +
            "WHERE i.technicianInfo.id = :technicianInfoId " +
            "AND (i.dateFin IS NULL OR i.dateFin > CURRENT_TIMESTAMP)") // Adjust based on your status logic
    boolean existsActiveByTechnicianInfoId(@Param("technicianInfoId") UUID technicianInfoId);
}
