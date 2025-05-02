package com.gestion.intervention.domain.person.services;

import com.gestion.intervention.domain.person.model.Person;
import com.gestion.intervention.domain.person.repository.PersonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PersonDetailsServiceImp implements PersonDetailsService {
    private final PersonRepository personRepository;

    @Override
    public Person loadUserByUsername(String username) {
        return personRepository.findByUsername(username).orElseThrow(null);
    }
}