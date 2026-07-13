package com.devcaiqueoliveira.nexus_api.service;

import com.devcaiqueoliveira.nexus_api.dto.StudySessionResponse;
import com.devcaiqueoliveira.nexus_api.dto.StudySessionStart;
import com.devcaiqueoliveira.nexus_api.entity.StudySession;
import com.devcaiqueoliveira.nexus_api.entity.Subject;
import com.devcaiqueoliveira.nexus_api.entity.enums.StudySessionStatus;
import com.devcaiqueoliveira.nexus_api.repository.StudySessionRepository;
import com.devcaiqueoliveira.nexus_api.repository.SubjectRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StudySessionService {

    private final StudySessionRepository studySessionRepository;
    private final SubjectRepository subjectRepository;

    @Transactional
    public StudySessionResponse startSession(StudySessionStart studySessionStart) {
        Subject subject = subjectRepository.findById(studySessionStart.subjectId())
                .orElseThrow(() -> new EntityNotFoundException("Disciplina não encontrada com o ID: " + studySessionStart.subjectId()));

        StudySession studySession = new StudySession(
                null,
                subject,
                LocalDateTime.now(),
                null,
                null,
                null
        );

        StudySession savedSession = studySessionRepository.save(studySession);

        return new StudySessionResponse(studySession);

    }

    @Transactional
    public StudySessionResponse finishSession(UUID sessionId) {

        StudySession studySession = studySessionRepository.findById(sessionId)
                .orElseThrow(() -> new EntityNotFoundException("Sessão de estudo não encontrada."));

        if (studySession.getStatus() != StudySessionStatus.IN_PROGRESS) {
            throw new IllegalStateException("Apenas sessões em andamento podem ser finalizadas. Status atual: " + studySession.getStatus());
        }

        studySession.setEndedAt(LocalDateTime.now());
        studySession.setStatus(StudySessionStatus.COMPLETED);

        StudySession updatedSession = studySessionRepository.save(studySession);

        return new StudySessionResponse(updatedSession);
    }

}
