package com.devcaiqueoliveira.nexus_api.service;

import com.devcaiqueoliveira.nexus_api.dto.StudySessionResponse;
import com.devcaiqueoliveira.nexus_api.dto.StudySessionStart;
import com.devcaiqueoliveira.nexus_api.entity.StudySession;
import com.devcaiqueoliveira.nexus_api.entity.Subject;
import com.devcaiqueoliveira.nexus_api.entity.User;
import com.devcaiqueoliveira.nexus_api.entity.enums.StudySessionStatus;
import com.devcaiqueoliveira.nexus_api.exception.exceptions.ForbiddenActionException;
import com.devcaiqueoliveira.nexus_api.repository.StudySessionRepository;
import com.devcaiqueoliveira.nexus_api.repository.SubjectRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class StudySessionServiceTest {

    @Mock
    private StudySessionRepository studySessionRepository;

    @Mock
    private SubjectRepository subjectRepository;

    @InjectMocks
    private StudySessionService studySessionService;

    private User createFakeUser(UUID userId) {
        User user = new User("Tester", "tester@teste.com", "123456");
        ReflectionTestUtils.setField(user, "id", userId);
        return user;
    }

    private Subject createFakeSubject(UUID subjectId, User user) {
        Subject subject = new Subject("Matéria", "Desc", 10, user);
        ReflectionTestUtils.setField(subject, "id", subjectId);
        return subject;
    }

    private StudySession createFakeSession(UUID sessionId, Subject subject, StudySessionStatus status) {
        StudySession session = new StudySession(subject, LocalDateTime.now().minusHours(1));
        ReflectionTestUtils.setField(session, "id", sessionId);
        ReflectionTestUtils.setField(session, "status", status);
        return session;
    }

    @Test
    @DisplayName("Deve iniciar uma sessão de estudo com sucesso")
    void startSessionSuccess() {
        UUID userId = UUID.randomUUID();
        UUID subjectId = UUID.randomUUID();
        User user = createFakeUser(userId);
        Subject subject = createFakeSubject(subjectId, user);
        StudySessionStart request = new StudySessionStart(subjectId);

        when(subjectRepository.findById(subjectId)).thenReturn(Optional.of(subject));
        when(studySessionRepository.existsBySubjectIdAndStatus(subjectId, StudySessionStatus.IN_PROGRESS)).thenReturn(false);
        when(studySessionRepository.save(any(StudySession.class))).thenAnswer(invocation -> {
            StudySession saved = invocation.getArgument(0);
            ReflectionTestUtils.setField(saved, "id", UUID.randomUUID());
            ReflectionTestUtils.setField(saved, "status", StudySessionStatus.IN_PROGRESS);
            return saved;
        });

        StudySessionResponse response = studySessionService.startSession(request, userId);

        assertNotNull(response);
        assertEquals(StudySessionStatus.IN_PROGRESS, response.status());
        verify(studySessionRepository, times(1)).save(any(StudySession.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao iniciar sessão com matéria inexistente")
    void startSessionSubjectNotFound() {
        UUID subjectId = UUID.randomUUID();
        StudySessionStart request = new StudySessionStart(subjectId);

        when(subjectRepository.findById(subjectId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            studySessionService.startSession(request, UUID.randomUUID());
        });
        verify(studySessionRepository, never()).save(any(StudySession.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao iniciar sessão se já existir uma em andamento")
    void startSessionAlreadyInProgress() {
        UUID userId = UUID.randomUUID();
        UUID subjectId = UUID.randomUUID();
        User user = createFakeUser(userId);
        Subject subject = createFakeSubject(subjectId, user);
        StudySessionStart request = new StudySessionStart(subjectId);

        when(subjectRepository.findById(subjectId)).thenReturn(Optional.of(subject));
        when(studySessionRepository.existsBySubjectIdAndStatus(subjectId, StudySessionStatus.IN_PROGRESS)).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> {
            studySessionService.startSession(request, userId);
        });
        verify(studySessionRepository, never()).save(any(StudySession.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao iniciar sessão de matéria de outro usuário (Forbidden)")
    void startSessionForbidden() {
        UUID userId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();
        UUID subjectId = UUID.randomUUID();
        User otherUser = createFakeUser(otherUserId);
        Subject subject = createFakeSubject(subjectId, otherUser);
        StudySessionStart request = new StudySessionStart(subjectId);

        when(subjectRepository.findById(subjectId)).thenReturn(Optional.of(subject));
        assertThrows(ForbiddenActionException.class, () -> {
            studySessionService.startSession(request, userId);
        });
        verify(studySessionRepository, never())
                .existsBySubjectIdAndStatus(any(UUID.class), any(StudySessionStatus.class));
        verify(studySessionRepository, never()).save(any(StudySession.class));
    }

    @Test
    @DisplayName("Deve finalizar uma sessão de estudo com sucesso")
    void finishSessionSuccess() {
        UUID userId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        User user = createFakeUser(userId);
        Subject subject = createFakeSubject(UUID.randomUUID(), user);
        StudySession session = createFakeSession(sessionId, subject, StudySessionStatus.IN_PROGRESS);

        when(studySessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
        when(studySessionRepository.save(any(StudySession.class))).thenReturn(session);

        StudySessionResponse response = studySessionService.finishSession(sessionId, userId);

        assertNotNull(response);
        assertEquals(StudySessionStatus.COMPLETED, response.status());
        assertNotNull(response.endedAt());
        verify(studySessionRepository, times(1)).save(any(StudySession.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao finalizar sessão inexistente")
    void finishSessionNotFound() {
        UUID sessionId = UUID.randomUUID();

        when(studySessionRepository.findById(sessionId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            studySessionService.finishSession(sessionId, UUID.randomUUID());
        });
    }

    @Test
    @DisplayName("Deve lançar exceção ao finalizar sessão de outro usuário (Forbidden)")
    void finishSessionForbidden() {
        UUID userId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        User otherUser = createFakeUser(otherUserId);
        Subject subject = createFakeSubject(UUID.randomUUID(), otherUser);
        StudySession session = createFakeSession(sessionId, subject, StudySessionStatus.IN_PROGRESS);

        when(studySessionRepository.findById(sessionId)).thenReturn(Optional.of(session));

        assertThrows(ForbiddenActionException.class, () -> {
            studySessionService.finishSession(sessionId, userId);
        });
    }

    @Test
    @DisplayName("Deve lançar exceção ao finalizar sessão que não está em andamento")
    void finishSessionNotInProgress() {
        UUID userId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        User user = createFakeUser(userId);
        Subject subject = createFakeSubject(UUID.randomUUID(), user);
        StudySession session = createFakeSession(sessionId, subject, StudySessionStatus.COMPLETED);

        when(studySessionRepository.findById(sessionId)).thenReturn(Optional.of(session));

        assertThrows(IllegalStateException.class, () -> {
            studySessionService.finishSession(sessionId, userId);
        });
    }

    @Test
    @DisplayName("Deve buscar sessão por ID com sucesso")
    void findByIdSuccess() {
        UUID userId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        User user = createFakeUser(userId);
        Subject subject = createFakeSubject(UUID.randomUUID(), user);
        StudySession session = createFakeSession(sessionId, subject, StudySessionStatus.COMPLETED);

        when(studySessionRepository.findById(sessionId)).thenReturn(Optional.of(session));

        StudySessionResponse response = studySessionService.findById(sessionId, userId);

        assertNotNull(response);
        assertEquals(sessionId, response.id());
    }

    @Test
    @DisplayName("Deve deletar sessão com sucesso")
    void deleteStudySessionSuccess() {
        UUID userId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        User user = createFakeUser(userId);
        Subject subject = createFakeSubject(UUID.randomUUID(), user);
        StudySession session = createFakeSession(sessionId, subject, StudySessionStatus.COMPLETED);

        when(studySessionRepository.findById(sessionId)).thenReturn(Optional.of(session));

        assertDoesNotThrow(() -> {
            studySessionService.deleteStudySession(sessionId, userId);
        });
        verify(studySessionRepository, times(1)).delete(session);
    }

    @Test
    @DisplayName("Deve listar sessões paginadas por subject com sucesso")
    void findAllBySubjectIdSuccess() {
        UUID userId = UUID.randomUUID();
        UUID subjectId = UUID.randomUUID();
        User user = createFakeUser(userId);
        Subject subject = createFakeSubject(subjectId, user);
        StudySession session = createFakeSession(UUID.randomUUID(), subject, StudySessionStatus.COMPLETED);

        Pageable pageable = PageRequest.of(0, 10);
        Page<StudySession> page = new PageImpl<>(List.of(session), pageable, 1);

        when(subjectRepository.findById(subjectId)).thenReturn(Optional.of(subject));
        when(studySessionRepository.findAllBySubjectId(subjectId, pageable)).thenReturn(page);

        Page<StudySessionResponse> responsePage = studySessionService.findAllBySubjectId(subjectId, pageable, userId);

        assertNotNull(responsePage);
        assertEquals(1, responsePage.getTotalElements());
    }

    @Test
    @DisplayName("Deve negar a listagem vazia de sessões de uma matéria pertencente a outro usuário")
    void findAllBySubjectIdForbidden() {
        UUID userId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();
        UUID subjectId = UUID.randomUUID();
        User otherUser = createFakeUser(otherUserId);
        Subject subject = createFakeSubject(subjectId, otherUser);
        Pageable pageable = PageRequest.of(0, 10);

        when(subjectRepository.findById(subjectId)).thenReturn(Optional.of(subject));

        assertThrows(ForbiddenActionException.class, () -> {
            studySessionService.findAllBySubjectId(subjectId, pageable, userId);
        });
        verify(studySessionRepository, never()).findAllBySubjectId(any(UUID.class), any(Pageable.class));
    }
}
