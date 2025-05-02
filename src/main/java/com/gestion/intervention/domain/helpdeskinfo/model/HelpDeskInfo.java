package com.gestion.intervention.domain.helpdeskinfo.model;

import com.gestion.intervention.domain.person.model.Person;
import com.gestion.intervention.domain.stemaintenance.model.STEmaintenance;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HelpDeskInfo {
    @Id
    @UuidGenerator
    private UUID id;

    @OneToOne
    @JoinColumn(name = "person_id")
    private Person person;

    @ManyToOne
    @JoinColumn(name = "ste_id")
    private STEmaintenance ste;
}

