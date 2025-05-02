package com.gestion.intervention.domain.person.repository;

import com.gestion.intervention.domain.person.model.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PersonRepository extends JpaRepository<Person, UUID> {
    Optional<Person> findByCIN(String cin);
    Optional<Person> findByEmail(String email);
    Optional<Person> findByUsername(String username);
    Optional<Person> findByTelephone(String telephone);
    @Query("SELECT COUNT(p) FROM Person p JOIN p.roles r WHERE r.id = :roleId") // Adjust 'p.roles' to your actual field name
    long countByRoleId(@Param("roleId") UUID roleId);
}
