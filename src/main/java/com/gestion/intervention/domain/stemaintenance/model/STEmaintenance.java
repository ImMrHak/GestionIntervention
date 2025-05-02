package com.gestion.intervention.domain.stemaintenance.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class STEmaintenance {
    @Id
    @UuidGenerator
    private UUID id;

    private String nom;
}
