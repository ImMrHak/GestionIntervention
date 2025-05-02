package com.gestion.intervention.domain.disponibile.model;

import com.gestion.intervention.domain.technicianinfo.model.TechnicianInfo;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Disponibilite {
    @Id
    @UuidGenerator
    private UUID id;

    private String etat;
    private LocalDateTime debut;
    private LocalDateTime fin;

    @ManyToOne
    @JoinColumn(name = "technician_info_id")
    private TechnicianInfo technicianInfo;
}

