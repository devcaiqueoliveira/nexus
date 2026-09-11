package com.devcaiqueoliveira.nexus_api.controller;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.devcaiqueoliveira.nexus_api.AbstractIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.time.Instant;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SecurityFilterIT extends AbstractIntegrationTest {

    private static final Algorithm TEST_ALGORITHM =
            Algorithm.HMAC256("test-only-jwt-secret-for-nexus-api");

    @Test
    @DisplayName("Deve retornar erro padronizado quando o token não for informado")
    void shouldRejectRequestWithoutToken() throws Exception {
        mockMvc.perform(get("/api/subjects"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.path").value("/api/subjects"));
    }

    @Test
    @DisplayName("Deve rejeitar header de autenticação fora do formato Bearer")
    void shouldRejectMalformedAuthorizationHeader() throws Exception {
        mockMvc.perform(get("/api/subjects")
                        .header(HttpHeaders.AUTHORIZATION, "Basic credentials"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Deve rejeitar token inválido")
    void shouldRejectInvalidToken() throws Exception {
        mockMvc.perform(get("/api/subjects")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer invalid.token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Deve rejeitar token de usuário inexistente")
    void shouldRejectTokenFromMissingUser() throws Exception {
        String token = createToken(UUID.randomUUID().toString(), Instant.now().plusSeconds(60));

        mockMvc.perform(get("/api/subjects")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Deve rejeitar token expirado")
    void shouldRejectExpiredToken() throws Exception {
        String token = createToken(UUID.randomUUID().toString(), Instant.now().minusSeconds(1));

        mockMvc.perform(get("/api/subjects")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    private String createToken(String subject, Instant expiresAt) {
        return JWT.create()
                .withIssuer("nexus-api")
                .withSubject(subject)
                .withExpiresAt(expiresAt)
                .sign(TEST_ALGORITHM);
    }
}
