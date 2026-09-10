package com.devcaiqueoliveira.nexus_api.service;

import com.devcaiqueoliveira.nexus_api.entity.User;
import com.devcaiqueoliveira.nexus_api.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuthenticationService authenticationService;

    @Test
    @DisplayName("Deve retornar UserDetails quando email for encontrado")
    void loadUserByUsernameSuccess() {
        String email = "teste@teste.com";
        User user = new User("Teste", email, "123456");

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        UserDetails userDetails = authenticationService.loadUserByUsername(email);

        assertNotNull(userDetails);
        assertEquals(email, userDetails.getUsername());
        verify(userRepository, times(1)).findByEmail(email);
    }

    @Test
    @DisplayName("Deve lançar UsernameNotFoundException quando email não for encontrado")
    void loadUserByUsernameNotFound() {
        String email = "notfound@teste.com";

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> {
            authenticationService.loadUserByUsername(email);
        });

        verify(userRepository, times(1)).findByEmail(email);
    }
}
