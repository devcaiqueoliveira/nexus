package com.devcaiqueoliveira.nexus_api.service;

import com.devcaiqueoliveira.nexus_api.dto.StudySessionResponse;
import com.devcaiqueoliveira.nexus_api.dto.StudySessionStart;
import com.devcaiqueoliveira.nexus_api.entity.StudySession;
import com.devcaiqueoliveira.nexus_api.entity.Subject;
import com.devcaiqueoliveira.nexus_api.entity.enums.StudySessionStatus;
import com.devcaiqueoliveira.nexus_api.exception.exceptions.ForbiddenActionException;
import com.devcaiqueoliveira.nexus_api.repository.StudySessionRepository;
import com.devcaiqueoliveira.nexus_api.repository.SubjectRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    public StudySessionResponse startSession(StudySessionStart studySessionStart, UUID loggedUserId) {

        Subject subject = subjectRepository.findById(studySessionStart.subjectId())
                .orElseThrow(() -> new EntityNotFoundException("Disciplina não encontrada"));

        if (studySessionRepository.existsBySubjectIdAndStatus(subject.getId(), StudySessionStatus.IN_PROGRESS)) {
            throw new IllegalStateException("Já existe uma sessão de estudos vigente");
        }

        StudySession studySession = new StudySession(
                subject,
                LocalDateTime.now()
        );

        if (!studySession.getSubject().getUser().getId().equals(loggedUserId)) {
            throw new ForbiddenActionException("Você não tem permissão para acessar esta sessão de estudos");
        }

        StudySession savedSession = studySessionRepository.save(studySession);
        return new StudySessionResponse(savedSession);

    }

    @Transactional
    public StudySessionResponse finishSession(UUID sessionId, UUID loggedUserId) {

        StudySession studySession = studySessionRepository.findById(sessionId)
                .orElseThrow(() -> new EntityNotFoundException("Sessão de estudos não encontrada"));

        if (!studySession.getSubject().getUser().getId().equals(loggedUserId)) {
            throw new ForbiddenActionException("Você não tem permissão para acessar esta sessão de estudos");
        }

        if (studySession.getStatus() != StudySessionStatus.IN_PROGRESS) {
            throw new IllegalStateException("Apenas sessões em andamento podem ser finalizadas. Status atual: " + studySession.getStatus());
        }

        studySession.setEndedAt(LocalDateTime.now());
        studySession.setStatus(StudySessionStatus.COMPLETED);

        StudySession updatedSession = studySessionRepository.save(studySession);

        return new StudySessionResponse(updatedSession);
    }

    public StudySessionResponse findById(UUID id, UUID loggedUserId) {
        StudySession studySession = studySessionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Sessão de estudos não encontrada"));

        if (!studySession.getSubject().getUser().getId().equals(loggedUserId)) {
            throw new ForbiddenActionException("Você não tem permissão para acessar essa sessão de estudos");
        }
        return new StudySessionResponse(studySession);
    }

    @Transactional
    public void deleteStudySession(UUID id, UUID loggedUserId) {
        StudySession studySession = studySessionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Sessão de estudos não encontrada"));

        if (!studySession.getSubject().getUser().getId().equals(loggedUserId)) {
            throw new ForbiddenActionException("Você não tem permissão para acessar esta sessão de estudos");
        }

        studySessionRepository.delete(studySession);
    }

    public Page<StudySessionResponse> findAllBySubjectId(UUID subjectId, Pageable pageable, UUID loggedUserId) {
        Page<StudySession> studySessions = studySessionRepository.findAllBySubjectId(subjectId, pageable);

        if (!studySessions.stream().allMatch(studySession ->
                studySession.getSubject().getUser().getId().equals(loggedUserId))) {
            throw new ForbiddenActionException("Você não tem permissão para acessar esta sessão de estudos");
        }
        return studySessions.map(StudySessionResponse::new);
    }

}
