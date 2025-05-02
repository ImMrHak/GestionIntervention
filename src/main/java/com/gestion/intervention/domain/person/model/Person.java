package com.gestion.intervention.domain.person.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.gestion.intervention.domain.role.model.Role;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Person implements UserDetails {
    @Id
    @UuidGenerator
    private UUID id;

    private String CIN;
    private String nom;
    private String prenom;
    private String email;
    private String username;
    @JsonIgnore
    private String password;
    private String telephone;
    private String address;
    private LocalDate dateNaissance;

    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(
            name = "person_role",
            joinColumns = @JoinColumn(name = "person_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private List<Role> roles;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return new HashSet<>(roles);
    }
}

