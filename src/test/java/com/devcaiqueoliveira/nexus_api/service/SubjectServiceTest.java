package com.devcaiqueoliveira.nexus_api.service;

import com.devcaiqueoliveira.nexus_api.dto.SubjectProgressResponse;
import com.devcaiqueoliveira.nexus_api.dto.SubjectRequest;
import com.devcaiqueoliveira.nexus_api.dto.SubjectResponse;
import com.devcaiqueoliveira.nexus_api.dto.SubjectUpdateRequest;
import com.devcaiqueoliveira.nexus_api.entity.StudySession;
import com.devcaiqueoliveira.nexus_api.entity.Subject;
import com.devcaiqueoliveira.nexus_api.entity.User;
import com.devcaiqueoliveira.nexus_api.entity.enums.StudySessionStatus;
import com.devcaiqueoliveira.nexus_api.exception.exceptions.ForbiddenActionException;
import com.devcaiqueoliveira.nexus_api.repository.StudySessionRepository;
import com.devcaiqueoliveira.nexus_api.repository.SubjectRepository;
import com.devcaiqueoliveira.nexus_api.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SubjectServiceTest  {

    @Mock
    private SubjectRepository subjectRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private StudySessionRepository studySessionRepository;

    @InjectMocks
    private SubjectService subjectService;

    private User createFakeUser(UUID userId) {
        User user = new User("Tester", "tester@teste.com", "123456");
        ReflectionTestUtils.setField(user, "id", userId);
        return user;
    }

    private Subject createFakeSubject(UUID subjectId, User user, Integer targetHours) {
        Subject subject = new Subject("Matéria Teste", "Estudos", targetHours, user);
        ReflectionTestUtils.setField(subject, "id", subjectId);
        return subject;
    }

    @Test
    @DisplayName("Deve criar uma matéria com sucesso")
    void createSubject() {

        UUID userId = UUID.randomUUID();
        User user = createFakeUser(userId);

        SubjectRequest request = new SubjectRequest("Matéria Teste", "Estudos de testes", 40);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(subjectRepository.save(any(Subject.class))).thenAnswer(invocationOnMock -> {
            Subject subjectToSave = invocationOnMock.getArgument(0);
            ReflectionTestUtils.setField(subjectToSave, "id", UUID.randomUUID());
            return subjectToSave;
        });

        SubjectResponse response = subjectService.createSubject(request, userId);

        assertNotNull(response);
        assertNotNull(response.id());
        assertEquals(request.name(), response.name());
        assertEquals(request.description(), response.description());
        assertEquals(request.targetHours(), response.targetHours());

        verify(userRepository, times(1)).findById(userId);
        verify(subjectRepository, times(1)).save(any(Subject.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar criar matéria com usuário inexistente")
    void createSubjectUserNotFound() {

        UUID userId = UUID.randomUUID();
        SubjectRequest request = new SubjectRequest("Matéria Teste", "Estudos", 40);

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            subjectService.createSubject(request, userId);
        });

        assertEquals("Usuário não encontrado", exception.getMessage());

        verify(userRepository, times(1)).findById(userId);
        verify(subjectRepository, never()).save(any(Subject.class));
    }

    @Test
    @DisplayName("Deve retornar lista de matérias do usuário")
    void findAllByUserId() {
        UUID userId = UUID.randomUUID();
        User user = createFakeUser(userId);
        Subject subject = createFakeSubject(UUID.randomUUID(), user, 40);
        
        when(subjectRepository.findAllByUserId(userId)).thenReturn(List.of(subject));
        
        List<SubjectResponse> responses = subjectService.findAllByUserId(userId);
        
        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(subject.getId(), responses.get(0).id());
        
        verify(subjectRepository, times(1)).findAllByUserId(userId);
    }
    
    @Test
    @DisplayName("Deve retornar lista vazia quando usuário não tem matérias")
    void findAllByUserIdEmptyList() {
        UUID userId = UUID.randomUUID();
        
        when(subjectRepository.findAllByUserId(userId)).thenReturn(Collections.emptyList());
        
        List<SubjectResponse> responses = subjectService.findAllByUserId(userId);
        
        assertNotNull(responses);
        assertTrue(responses.isEmpty());
        
        verify(subjectRepository, times(1)).findAllByUserId(userId);
    }

    @Test
    @DisplayName("Deve retornar matéria por ID com sucesso")
    void findByIdSuccess() {
        UUID userId = UUID.randomUUID();
        UUID subjectId = UUID.randomUUID();
        User user = createFakeUser(userId);
        Subject subject = createFakeSubject(subjectId, user, 40);

        when(subjectRepository.findById(subjectId)).thenReturn(Optional.of(subject));

        SubjectResponse response = subjectService.findById(subjectId, userId);

        assertNotNull(response);
        assertEquals(subjectId, response.id());
        
        verify(subjectRepository, times(1)).findById(subjectId);
    }

    @Test
    @DisplayName("Deve lançar exceção ao buscar matéria que não existe")
    void findByIdNotFound() {
        UUID userId = UUID.randomUUID();
        UUID subjectId = UUID.randomUUID();

        when(subjectRepository.findById(subjectId)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            subjectService.findById(subjectId, userId);
        });

        assertEquals("Matéria não encontrada", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar exceção ao buscar matéria de outro usuário (Forbidden)")
    void findByIdForbidden() {
        UUID userId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();
        UUID subjectId = UUID.randomUUID();
        User otherUser = createFakeUser(otherUserId);
        Subject subject = createFakeSubject(subjectId, otherUser, 40);

        when(subjectRepository.findById(subjectId)).thenReturn(Optional.of(subject));

        ForbiddenActionException exception = assertThrows(ForbiddenActionException.class, () -> {
            subjectService.findById(subjectId, userId);
        });

        assertEquals("Você não tem permissão para acessar esta matéria", exception.getMessage());
    }

    @Test
    @DisplayName("Deve atualizar matéria com sucesso")
    void updateSubjectSuccess() {
        UUID userId = UUID.randomUUID();
        UUID subjectId = UUID.randomUUID();
        User user = createFakeUser(userId);
        Subject subject = createFakeSubject(subjectId, user, 40);

        SubjectUpdateRequest request = new SubjectUpdateRequest("Novo Nome", "Nova Descrição", 50);

        when(subjectRepository.findById(subjectId)).thenReturn(Optional.of(subject));
        when(subjectRepository.save(any(Subject.class))).thenReturn(subject);

        SubjectResponse response = subjectService.updateSubject(subjectId, request, userId);

        assertNotNull(response);
        assertEquals("Novo Nome", response.name());
        assertEquals("Nova Descrição", response.description());
        assertEquals(50, response.targetHours());

        verify(subjectRepository, times(1)).save(any(Subject.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar atualizar matéria de outro usuário (Forbidden)")
    void updateSubjectForbidden() {
        UUID userId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();
        UUID subjectId = UUID.randomUUID();
        User otherUser = createFakeUser(otherUserId);
        Subject subject = createFakeSubject(subjectId, otherUser, 40);

        SubjectUpdateRequest request = new SubjectUpdateRequest("Novo Nome", "Nova Descrição", 50);

        when(subjectRepository.findById(subjectId)).thenReturn(Optional.of(subject));

        ForbiddenActionException exception = assertThrows(ForbiddenActionException.class, () -> {
            subjectService.updateSubject(subjectId, request, userId);
        });

        assertEquals("Você não tem permissão para acessar esta matéria", exception.getMessage());
        verify(subjectRepository, never()).save(any(Subject.class));
    }


    @Test
    @DisplayName("Deve deletar matéria com sucesso")
    void deleteSubjectSuccess() {
        UUID userId = UUID.randomUUID();
        UUID subjectId = UUID.randomUUID();
        User user = createFakeUser(userId);
        Subject subject = createFakeSubject(subjectId, user, 40);

        when(subjectRepository.findById(subjectId)).thenReturn(Optional.of(subject));

        assertDoesNotThrow(() -> {
            subjectService.deleteSubject(subjectId, userId);
        });

        verify(subjectRepository, times(1)).delete(subject);
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar deletar matéria de outro usuário (Forbidden)")
    void deleteSubjectForbidden() {
        UUID userId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();
        UUID subjectId = UUID.randomUUID();
        User otherUser = createFakeUser(otherUserId);
        Subject subject = createFakeSubject(subjectId, otherUser, 40);

        when(subjectRepository.findById(subjectId)).thenReturn(Optional.of(subject));

        ForbiddenActionException exception = assertThrows(ForbiddenActionException.class, () -> {
            subjectService.deleteSubject(subjectId, userId);
        });

        assertEquals("Você não tem permissão para acessar esta matéria", exception.getMessage());
        verify(subjectRepository, never()).delete(any(Subject.class));
    }

    @Test
    @DisplayName("Deve calcular progresso da matéria com sucesso")
    void getSubjectProgressSuccess() {
        UUID userId = UUID.randomUUID();
        UUID subjectId = UUID.randomUUID();
        User user = createFakeUser(userId);
        Subject subject = createFakeSubject(subjectId, user, 10);

        StudySession session1 = new StudySession();
        ReflectionTestUtils.setField(session1, "status", StudySessionStatus.COMPLETED);
        ReflectionTestUtils.setField(session1, "startedAt", LocalDateTime.now().minusHours(3));
        ReflectionTestUtils.setField(session1, "endedAt", LocalDateTime.now());

        StudySession session2 = new StudySession();
        ReflectionTestUtils.setField(session2, "status", StudySessionStatus.COMPLETED);
        ReflectionTestUtils.setField(session2, "startedAt", LocalDateTime.now().minusHours(2));
        ReflectionTestUtils.setField(session2, "endedAt", LocalDateTime.now());

        when(subjectRepository.findById(subjectId)).thenReturn(Optional.of(subject));
        when(studySessionRepository.findAllBySubjectIdAndStatus(subjectId, StudySessionStatus.COMPLETED))
                .thenReturn(List.of(session1, session2));

        SubjectProgressResponse response = subjectService.getSubjectProgress(subjectId, userId);

        assertNotNull(response);
        assertEquals(10, response.targetHours());
        assertEquals(5.0, response.totalHoursStudied());
        assertEquals(5.0, response.remainingHours());
        assertEquals(50.0, response.completionPercentage());
    }

    @Test
    @DisplayName("Deve calcular progresso com horas estudadas excedendo o alvo")
    void getSubjectProgressExceedingTarget() {
        UUID userId = UUID.randomUUID();
        UUID subjectId = UUID.randomUUID();
        User user = createFakeUser(userId);
        Subject subject = createFakeSubject(subjectId, user, 2);

        StudySession session = new StudySession();
        ReflectionTestUtils.setField(session, "status", StudySessionStatus.COMPLETED);
        ReflectionTestUtils.setField(session, "startedAt", LocalDateTime.now().minusHours(3));
        ReflectionTestUtils.setField(session, "endedAt", LocalDateTime.now());

        when(subjectRepository.findById(subjectId)).thenReturn(Optional.of(subject));
        when(studySessionRepository.findAllBySubjectIdAndStatus(subjectId, StudySessionStatus.COMPLETED))
                .thenReturn(List.of(session));

        SubjectProgressResponse response = subjectService.getSubjectProgress(subjectId, userId);

        assertNotNull(response);
        assertEquals(2, response.targetHours());
        assertEquals(3.0, response.totalHoursStudied());
        assertEquals(0.0, response.remainingHours());
        assertEquals(100.0, response.completionPercentage());
    }
    
    @Test
    @DisplayName("Deve calcular progresso quando targetHours é nulo")
    void getSubjectProgressNullTarget() {
        UUID userId = UUID.randomUUID();
        UUID subjectId = UUID.randomUUID();
        User user = createFakeUser(userId);
        Subject subject = createFakeSubject(subjectId, user, null);

        StudySession session = new StudySession();
        ReflectionTestUtils.setField(session, "status", StudySessionStatus.COMPLETED);
        ReflectionTestUtils.setField(session, "startedAt", LocalDateTime.now().minusHours(1));
        ReflectionTestUtils.setField(session, "endedAt", LocalDateTime.now());

        when(subjectRepository.findById(subjectId)).thenReturn(Optional.of(subject));
        when(studySessionRepository.findAllBySubjectIdAndStatus(subjectId, StudySessionStatus.COMPLETED))
                .thenReturn(List.of(session));

        SubjectProgressResponse response = subjectService.getSubjectProgress(subjectId, userId);

        assertNotNull(response);
        assertEquals(0, response.targetHours());
        assertEquals(1.0, response.totalHoursStudied());
        assertEquals(0.0, response.remainingHours()); 
        assertEquals(0.0, response.completionPercentage());
    }
}
