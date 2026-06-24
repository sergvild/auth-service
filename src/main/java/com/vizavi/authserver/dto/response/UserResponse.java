package com.vizavi.authserver.dto.response;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String email,
        Set<String> roles,
        Instant createdAt
) {}
