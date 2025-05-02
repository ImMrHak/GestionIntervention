package com.gestion.intervention.domain.disponibile.repository;

import com.gestion.intervention.domain.disponibile.model.Disponibilite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface DisponibiliteRepository extends JpaRepository<Disponibilite, UUID> {
    @Query("SELECT d FROM Disponibilite d " +
            "WHERE d.technicianInfo.id = :technicianInfoId " +
            "AND d.debut < :newFin " +
            "AND d.fin > :newDebut " +
            "AND (:idToExclude IS NULL OR d.id <> :idToExclude)")
    List<Disponibilite> findOverlapping(
            @Param("technicianInfoId") UUID technicianInfoId,
            @Param("newDebut") LocalDateTime newDebut,
            @Param("newFin") LocalDateTime newFin,
            @Param("idToExclude") UUID idToExclude);
    boolean existsByTechnicianInfoId(UUID technicianInfoId);
}
