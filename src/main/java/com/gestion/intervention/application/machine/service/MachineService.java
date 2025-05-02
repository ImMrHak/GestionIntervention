package com.gestion.intervention.application.machine.service;

import com.gestion.intervention.application.machine.record.MachineDTO;

import java.util.List;
import java.util.UUID;

public interface MachineService {
    MachineDTO createMachine(MachineDTO dto);
    MachineDTO getMachineById(UUID id);
    List<MachineDTO> getAllMachines();
    MachineDTO updateMachine(UUID id, MachineDTO dto);
    void deleteMachine(UUID id);
}

