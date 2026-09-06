package com.devcaiqueoliveira.nexus_api.service;

import com.devcaiqueoliveira.nexus_api.dto.SubjectRequest;
import com.devcaiqueoliveira.nexus_api.dto.SubjectResponse;
import com.devcaiqueoliveira.nexus_api.entity.Subject;
import com.devcaiqueoliveira.nexus_api.entity.User;
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

    @Test
    @DisplayName("Deve criar uma matéria com sucesso")
    void createSubject() {

        UUID userId = UUID.randomUUID();
        User user = new User("Tester", "tester@teste.com", "123456");
        ReflectionTestUtils.setField(user, "id", userId);

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
}
