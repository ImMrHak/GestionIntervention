package com.gestion.intervention.application.person.service;

import com.gestion.intervention.application.person.record.PersonDTO;

import java.util.List;
import java.util.UUID;

public interface PersonService {
    PersonDTO createPerson(PersonDTO dto);
    PersonDTO getPersonById(UUID id);
    List<PersonDTO> getAllPersons();
    PersonDTO updatePerson(UUID id, PersonDTO dto);
    void deletePerson(UUID id);
}
