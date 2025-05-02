package com.gestion.intervention.domain.technicianinfo.model;

import com.gestion.intervention.domain.disponibile.model.Disponibilite;
import com.gestion.intervention.domain.person.model.Person;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TechnicianInfo {
    @Id
    @UuidGenerator
    private UUID id;

    @OneToOne
    @JoinColumn(name = "person_id")
    private Person person;

    private String idSte;
    private String specialite;
    private int nbrPanne;
    private int nbrPanneRegle;

    @OneToMany(mappedBy = "technicianInfo", cascade = CascadeType.ALL)
    private List<Disponibilite> disponibilites;
}

