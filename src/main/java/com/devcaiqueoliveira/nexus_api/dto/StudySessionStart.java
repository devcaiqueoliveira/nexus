package com.devcaiqueoliveira.nexus_api.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record StudySessionStart(
        @NotNull(message = "{studySession.subjectId.required}")
        UUID subjectId
) {
}
