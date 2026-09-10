package com.devcaiqueoliveira.nexus_api.controller;

import com.devcaiqueoliveira.nexus_api.AbstractIntegrationTest;
import com.devcaiqueoliveira.nexus_api.dto.AuthenticationRequest;
import com.devcaiqueoliveira.nexus_api.dto.UserRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AuthenticationControllerIT extends AbstractIntegrationTest {

    @Test
    @DisplayName("Deve registrar um novo usuário com sucesso")
    void shouldRegisterNewUser() throws Exception {
        UserRequest userRequest = new UserRequest("Teste", "teste@teste.com", "12345678");

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.name").value("Teste"))
                .andExpect(jsonPath("$.email").value("teste@teste.com"))
                .andExpect(header().exists("Location"));
    }

    @Test
    @DisplayName("Deve autenticar o usuário e retornar o token JWT com sucesso")
    void shouldAuthenticateUser() throws Exception {
        UserRequest userRequest = new UserRequest("Teste", "teste@teste.com", "12345678");
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isCreated());

        AuthenticationRequest authRequest = new AuthenticationRequest("teste@teste.com", "12345678");
        
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    @DisplayName("Deve falhar a autenticação ao usar senha incorreta")
    void shouldFailAuthenticationWithWrongPassword() throws Exception {
        UserRequest userRequest = new UserRequest("Teste", "teste@teste.com", "12345678");
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isCreated());

        AuthenticationRequest authRequest = new AuthenticationRequest("teste@teste.com", "senhaIncorreta");
        
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isForbidden());
    }
}
