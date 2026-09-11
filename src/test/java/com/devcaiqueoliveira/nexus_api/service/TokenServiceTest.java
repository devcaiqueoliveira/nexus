package com.devcaiqueoliveira.nexus_api.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.devcaiqueoliveira.nexus_api.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class TokenServiceTest {

    private TokenService tokenService;

    @BeforeEach
    void setUp() {
        tokenService = new TokenService("my-secret-key");
    }

    @Test
    @DisplayName("Deve gerar um token JWT válido")
    void generateTokenSuccess() {
        UUID id = UUID.randomUUID();
        User user = new User("Teste", "teste@teste.com", "123456");
        ReflectionTestUtils.setField(user, "id", id);

        String token = tokenService.generateToken(user);

        assertNotNull(token);
        assertFalse(token.isEmpty());

        Algorithm algorithm = Algorithm.HMAC256("my-secret-key");
        String subject = JWT.require(algorithm)
                .withIssuer("nexus-api")
                .build()
                .verify(token)
                .getSubject();

        assertEquals(id.toString(), subject);
    }

    @Test
    @DisplayName("Deve validar o token e retornar o subject (ID do usuário)")
    void validationTokenSuccess() {
        UUID id = UUID.randomUUID();
        User user = new User("Teste", "teste@teste.com", "123456");
        ReflectionTestUtils.setField(user, "id", id);

        String token = tokenService.generateToken(user);

        Optional<UUID> userId = tokenService.validateAndGetUserId(token);

        assertEquals(Optional.of(id), userId);
    }

    @Test
    @DisplayName("Deve retornar string vazia ao validar um token inválido")
    void validationTokenInvalid() {
        String invalidToken = "invalid.token.here";

        Optional<UUID> userId = tokenService.validateAndGetUserId(invalidToken);

        assertTrue(userId.isEmpty());
    }

    @Test
    @DisplayName("Deve retornar string vazia ao validar um token com assinatura incorreta")
    void validationTokenWrongSignature() {
        UUID id = UUID.randomUUID();
        User user = new User("Teste", "teste@teste.com", "123456");
        ReflectionTestUtils.setField(user, "id", id);

        TokenService anotherTokenService = new TokenService("wrong-secret-key");
        String tokenWithWrongSignature = anotherTokenService.generateToken(user);

        Optional<UUID> userId = tokenService.validateAndGetUserId(tokenWithWrongSignature);

        assertTrue(userId.isEmpty());
    }

    @Test
    @DisplayName("Deve rejeitar token expirado")
    void validationTokenExpired() {
        String expiredToken = JWT.create()
                .withIssuer("nexus-api")
                .withSubject(UUID.randomUUID().toString())
                .withExpiresAt(Instant.now().minusSeconds(1))
                .sign(Algorithm.HMAC256("my-secret-key"));

        assertTrue(tokenService.validateAndGetUserId(expiredToken).isEmpty());
    }

    @Test
    @DisplayName("Deve rejeitar token com subject que não é UUID")
    void validationTokenInvalidSubject() {
        String token = JWT.create()
                .withIssuer("nexus-api")
                .withSubject("invalid-user-id")
                .withExpiresAt(Instant.now().plusSeconds(60))
                .sign(Algorithm.HMAC256("my-secret-key"));

        assertTrue(tokenService.validateAndGetUserId(token).isEmpty());
    }

    @Test
    @DisplayName("Deve rejeitar token sem subject")
    void validationTokenWithoutSubject() {
        String token = JWT.create()
                .withIssuer("nexus-api")
                .withExpiresAt(Instant.now().plusSeconds(60))
                .sign(Algorithm.HMAC256("my-secret-key"));

        assertTrue(tokenService.validateAndGetUserId(token).isEmpty());
    }

    @Test
    @DisplayName("Deve rejeitar segredo JWT vazio")
    void blankSecret() {
        assertThrows(IllegalArgumentException.class, () -> new TokenService(" "));
    }
}
