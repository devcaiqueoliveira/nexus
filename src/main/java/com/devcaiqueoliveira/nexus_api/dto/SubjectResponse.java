package com.devcaiqueoliveira.nexus_api.dto;

import com.devcaiqueoliveira.nexus_api.entity.Subject;

import java.util.UUID;

public record SubjectResponse(
        UUID id,
        String name,
        String description,
        Integer targetHours,
        UUID userId
) {

    public SubjectResponse(Subject subject) {
        this(
                subject.getId(),
                subject.getName(),
                subject.getDescription(),
                subject.getTargetHours(),
                subject.getUser().getId()
        );
    }
}
