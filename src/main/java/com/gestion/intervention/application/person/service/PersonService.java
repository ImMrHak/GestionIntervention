package com.gestion.intervention.application.person.service;

import com.gestion.intervention.application.person.record.PersonDTO;
import com.gestion.intervention.application.person.record.request.LoginRequestDTO;
import com.gestion.intervention.application.person.record.response.LoginResponseDTO;

import java.util.List;
import java.util.UUID;

public interface PersonService {
    PersonDTO createPerson(PersonDTO dto);
    PersonDTO getPersonById(UUID id);
    List<PersonDTO> getAllPersons();
    PersonDTO updatePerson(UUID id, PersonDTO dto);
    void deletePerson(UUID id);

    // 000000
    LoginResponseDTO login(LoginRequestDTO dto);
    LoginResponseDTO register(PersonDTO dto);
}
