package com.gestion.intervention.domain.machine.repository;

import com.gestion.intervention.domain.machine.model.Machine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface MachineRepository extends JpaRepository<Machine, UUID> {
    boolean existsByType(String type);
}
