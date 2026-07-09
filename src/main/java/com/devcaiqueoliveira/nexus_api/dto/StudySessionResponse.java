package com.devcaiqueoliveira.nexus_api.dto;

import com.devcaiqueoliveira.nexus_api.entity.StudySession;
import com.devcaiqueoliveira.nexus_api.entity.enums.StudySessionStatus;
import com.devcaiqueoliveira.nexus_api.service.StudySessionService;

import java.time.LocalDateTime;
import java.util.UUID;

public record StudySessionResponse(
        UUID id,
        UUID subjectId,
        String subjectName,
        LocalDateTime startedAt,
        LocalDateTime endedAt,
        StudySessionStatus status
) {
    public StudySessionResponse(StudySession studySession) {
        this(
                studySession.getId(),
                studySession.getSubject().getId(),
                studySession.getSubject().getName(),
                studySession.getStartedAt(),
                studySession.getEndedAt(),
                studySession.getStatus()
        );
    }
}
