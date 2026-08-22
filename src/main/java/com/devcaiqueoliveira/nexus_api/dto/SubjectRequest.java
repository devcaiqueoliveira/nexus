package com.devcaiqueoliveira.nexus_api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record SubjectRequest(
        @NotBlank(message = "{subject.name.required}")
        @Size(max = 100, message = "{subject.name.size}")
        String name,

        String description,

        @NotNull(message = "{subject.targetHours.required}")
        @Positive(message = "{subject.targetHours.positive}")
        Integer targetHours,

        @NotNull(message = "{subject.userId.required}")
        UUID userId
) {
}
