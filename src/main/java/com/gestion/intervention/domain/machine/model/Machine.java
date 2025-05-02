package com.gestion.intervention.domain.machine.model;

import com.gestion.intervention.domain.panne.model.Panne;
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
public class Machine {
    @Id
    @UuidGenerator
    private UUID id;

    private String type;
    private String etat;

    @OneToMany(mappedBy = "machine")
    private List<Piece> pieces;

    @OneToMany(mappedBy = "machine")
    private List<Panne> pannes;
}

