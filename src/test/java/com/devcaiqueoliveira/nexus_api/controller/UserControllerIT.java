package com.devcaiqueoliveira.nexus_api.controller;

import com.devcaiqueoliveira.nexus_api.AbstractIntegrationTest;
import com.devcaiqueoliveira.nexus_api.dto.AuthenticationRequest;
import com.devcaiqueoliveira.nexus_api.dto.UserRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class UserControllerIT extends AbstractIntegrationTest {

    @Test
    @DisplayName("Deve retornar os dados do perfil do usuário autenticado via /me")
    void shouldGetAuthenticatedUserProfile() throws Exception {
        RegisteredUser user = registerAndAuthenticate("user.me@nexus.test", "User Me");

        mockMvc.perform(get("/api/users/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + user.token()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(user.id().toString()))
                .andExpect(jsonPath("$.name").value("User Me"))
                .andExpect(jsonPath("$.email").value("user.me@nexus.test"));
    }

    @Test
    @DisplayName("Deve rejeitar consulta ao /me sem token de autenticação")
    void shouldRejectProfileAccessWithoutToken() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    @Test
    @DisplayName("Deve permitir que o usuário consulte seu próprio perfil por ID")
    void shouldAllowUserToGetOwnProfileById() throws Exception {
        RegisteredUser user = registerAndAuthenticate("owner@nexus.test", "Owner User");

        mockMvc.perform(get("/api/users/{id}", user.id())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + user.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(user.id().toString()))
                .andExpect(jsonPath("$.name").value("Owner User"))
                .andExpect(jsonPath("$.email").value("owner@nexus.test"));
    }

    @Test
    @DisplayName("Deve negar acesso ao tentar consultar os dados de outro usuário por ID (403 Forbidden)")
    void shouldDenyAccessToAnotherUserProfileById() throws Exception {
        RegisteredUser victim = registerAndAuthenticate("victim@nexus.test", "Victim User");
        RegisteredUser attacker = registerAndAuthenticate("attacker@nexus.test", "Attacker User");

        mockMvc.perform(get("/api/users/{id}", victim.id())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + attacker.token()))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden Action"))
                .andExpect(jsonPath("$.message").value("Você não tem permissão para acessar os dados deste usuário"));
    }

    @Test
    @DisplayName("Deve rejeitar consulta por ID sem token de autenticação")
    void shouldRejectGetByIdWithoutToken() throws Exception {
        mockMvc.perform(get("/api/users/{id}", UUID.randomUUID()))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(401));
    }

    private RegisteredUser registerAndAuthenticate(String email, String name) throws Exception {
        UserRequest userRequest = new UserRequest(name, email, "12345678");

        MvcResult registerResult = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        UUID id = UUID.fromString(objectMapper.readTree(registerResult.getResponse().getContentAsString())
                .get("id")
                .asText());

        AuthenticationRequest authenticationRequest = new AuthenticationRequest(email, "12345678");

        MvcResult authenticationResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authenticationRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String token = objectMapper.readTree(authenticationResult.getResponse().getContentAsString())
                .get("token")
                .asText();

        return new RegisteredUser(id, token);
    }

    private record RegisteredUser(UUID id, String token) {}
}
