package com.gestion.intervention.domain.intervention.model;

import com.gestion.intervention.domain.panne.model.Panne;
import com.gestion.intervention.domain.technicianinfo.model.TechnicianInfo;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Intervention {
    @Id
    @UuidGenerator
    private UUID id;

    private LocalDateTime dateDebut;
    private LocalDateTime dateFin;
    private Duration duree;

    @ManyToOne
    @JoinColumn(name = "technician_id")
    private TechnicianInfo technicianInfo;

    @ManyToOne
    @JoinColumn(name = "panne_id")
    private Panne panne;
}
