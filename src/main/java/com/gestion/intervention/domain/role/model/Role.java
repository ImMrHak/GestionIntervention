package com.gestion.intervention.domain.role.model;

import com.gestion.intervention.domain.person.model.Person;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;

import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role implements GrantedAuthority {
    @Id
    @GeneratedValue
    private UUID id;

    private String authority;

    @ManyToMany(mappedBy = "roles")
    private List<Person> persons;
}