package com.devcaiqueoliveira.nexus_api.dto;

import java.util.UUID;

public record SubjectRequest(
        String name,
        String description,
        Integer targetHours,
        UUID userId
) {
}
