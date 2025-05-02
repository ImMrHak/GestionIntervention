package com.gestion.intervention.domain.piece.model;

import com.gestion.intervention.domain.machine.model.Machine;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Piece {
    @Id
    @UuidGenerator
    private UUID id;

    private String numPiece;
    private String nomPiece;
    private float prix;

    private String description;

    @ManyToOne
    @JoinColumn(name = "machine_id")
    private Machine machine;
}
