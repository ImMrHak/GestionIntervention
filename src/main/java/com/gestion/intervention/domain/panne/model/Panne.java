package com.gestion.intervention.domain.panne.model;

import com.gestion.intervention.domain.intervention.model.Intervention;
import com.gestion.intervention.domain.machine.model.Machine;
import com.gestion.intervention.domain.panne.enumeration.PanneStatus;
import com.gestion.intervention.domain.person.model.Person;
import com.gestion.intervention.domain.piece.model.Piece;
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
public class Panne {
    @Id
    @UuidGenerator
    private UUID id;

    @Enumerated(EnumType.STRING)
    private PanneStatus status;

    private String typePanne;

    @ManyToOne
    @JoinColumn(name = "machine_id")
    private Machine machine;

    @ManyToOne
    @JoinColumn(name = "person_id") // the one who reported the issue
    private Person reporter;

    @OneToMany(mappedBy = "panne")
    private List<Intervention> interventions;
}
