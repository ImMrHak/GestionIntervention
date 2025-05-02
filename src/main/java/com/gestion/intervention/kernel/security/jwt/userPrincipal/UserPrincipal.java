package com.gestion.intervention.kernel.security.jwt.userPrincipal;

import java.util.UUID;

public record UserPrincipal(UUID id, String email) {
}
