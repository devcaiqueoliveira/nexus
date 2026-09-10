package com.devcaiqueoliveira.nexus_api.service;

import com.devcaiqueoliveira.nexus_api.dto.UserRequest;
import com.devcaiqueoliveira.nexus_api.dto.UserResponse;
import com.devcaiqueoliveira.nexus_api.entity.User;
import com.devcaiqueoliveira.nexus_api.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

import java.util.UUID;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("Deve criar um usúario com sucesso")
    void createUserTest() {
        UserRequest request = new UserRequest("Teste", "teste@teste.com", "123456");

        when(userRepository.existsByEmail(request.email())).thenReturn(false);

        when(passwordEncoder.encode(request.password())).thenReturn("senhacriptografada");

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            ReflectionTestUtils.setField(savedUser, "id", UUID.randomUUID());
            return savedUser;
        });

        UserResponse response = userService.createUser(request);

        assertNotNull(response.id());

        assertEquals(request.name(), response.name());
        assertEquals(request.email(), response.email());

        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar criar usuário com e-mail duplicado")
    void createUserDuplicateEmailTest() {
        UserRequest request = new UserRequest("Teste", "teste@teste.com", "123456");

        when(userRepository.existsByEmail(request.email())).thenReturn(true);

        assertThrows(com.devcaiqueoliveira.nexus_api.exception.exceptions.DuplicateResourceException.class, () -> {
            userService.createUser(request);
        });

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Deve buscar um usuário por ID com sucesso")
    void findByIdTest() {
        UUID id = UUID.randomUUID();
        User user = new User("Teste", "teste@teste.com", "123456");
        ReflectionTestUtils.setField(user, "id", id);

        when(userRepository.findById(id)).thenReturn(java.util.Optional.of(user));

        UserResponse response = userService.findById(id);

        assertNotNull(response);
        assertEquals(id, response.id());
        assertEquals("Teste", response.name());
        assertEquals("teste@teste.com", response.email());
    }

    @Test
    @DisplayName("Deve lançar exceção ao buscar usuário inexistente por ID")
    void findByIdNotFoundTest() {
        UUID id = UUID.randomUUID();

        when(userRepository.findById(id)).thenReturn(java.util.Optional.empty());

        assertThrows(jakarta.persistence.EntityNotFoundException.class, () -> {
            userService.findById(id);
        });
    }

}
