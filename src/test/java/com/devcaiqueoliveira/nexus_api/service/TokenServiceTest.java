package com.devcaiqueoliveira.nexus_api.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.devcaiqueoliveira.nexus_api.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class TokenServiceTest {

    private TokenService tokenService;

    @BeforeEach
    void setUp() {
        tokenService = new TokenService();
        ReflectionTestUtils.setField(tokenService, "secret", "my-secret-key");
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

        String subject = tokenService.validationToken(token);

        assertNotNull(subject);
        assertEquals(id.toString(), subject);
    }

    @Test
    @DisplayName("Deve retornar string vazia ao validar um token inválido")
    void validationTokenInvalid() {
        String invalidToken = "invalid.token.here";

        String subject = tokenService.validationToken(invalidToken);

        assertNotNull(subject);
        assertEquals("", subject);
    }

    @Test
    @DisplayName("Deve retornar string vazia ao validar um token com assinatura incorreta")
    void validationTokenWrongSignature() {
        UUID id = UUID.randomUUID();
        User user = new User("Teste", "teste@teste.com", "123456");
        ReflectionTestUtils.setField(user, "id", id);

        TokenService anotherTokenService = new TokenService();
        ReflectionTestUtils.setField(anotherTokenService, "secret", "wrong-secret-key");
        String tokenWithWrongSignature = anotherTokenService.generateToken(user);

        String subject = tokenService.validationToken(tokenWithWrongSignature);

        assertNotNull(subject);
        assertEquals("", subject);
    }
}
