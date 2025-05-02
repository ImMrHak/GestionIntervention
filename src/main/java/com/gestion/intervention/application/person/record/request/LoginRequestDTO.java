package com.gestion.intervention.application.person.record.request;

public record LoginRequestDTO(
        String usernameOrEmail,

        String password
) {
}