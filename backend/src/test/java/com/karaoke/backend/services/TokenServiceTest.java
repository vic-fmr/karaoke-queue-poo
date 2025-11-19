package com.karaoke.backend.services;

import com.karaoke.backend.models.User;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.Key;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class TokenServiceTest {

    @InjectMocks
    private TokenService tokenService;

    private User testUser;
    private String secretKeyBase64;

    @BeforeEach
    void setUp() {
        // Gera uma chave válida para o algoritmo HS256
        Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
        secretKeyBase64 = Encoders.BASE64.encode(key.getEncoded());

        // Injeta o segredo no serviço
        ReflectionTestUtils.setField(tokenService, "secret", secretKeyBase64);
        
        testUser = new User();
        testUser.setId(1L); // É bom ter ID também
        testUser.setUsername("testuser");
        // CORREÇÃO 1: O seu TokenService usa o EMAIL como Subject, então precisamos setar ele
        testUser.setEmail("teste@email.com"); 
    }

    @Test
    void generateToken_ShouldCreateValidToken() {
        ReflectionTestUtils.setField(tokenService, "expirationTime", 60000L);

        String token = tokenService.generateToken(testUser);

        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void validateToken_ShouldReturnUsername_WhenTokenIsValid() {
        ReflectionTestUtils.setField(tokenService, "expirationTime", 60000L);
        String token = tokenService.generateToken(testUser);

        // O método retorna o "Subject" do token
        String subject = tokenService.validateToken(token);

        // CORREÇÃO 2: Seu serviço coloca o email no subject (.setSubject(user.getEmail())),
        // então devemos esperar o email de volta, não o username.
        assertEquals("teste@email.com", subject);
    }

    @Test
    void validateToken_ShouldReturnEmptyString_WhenTokenIsInvalid() {
        ReflectionTestUtils.setField(tokenService, "expirationTime", 60000L);

        String username = tokenService.validateToken("invalid.token.string");

        assertEquals("", username);
    }
    
    @Test
    void validateToken_ShouldReturnEmptyString_WhenTokenIsSignedWithDifferentKey() {
        ReflectionTestUtils.setField(tokenService, "expirationTime", 60000L);
        String token = tokenService.generateToken(testUser);

        // Gera uma outra chave aleatória para simular erro de assinatura
        Key otherKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);
        String otherBase64Key = Encoders.BASE64.encode(otherKey.getEncoded());
        
        // Troca a chave do serviço para tentar validar o token antigo
        ReflectionTestUtils.setField(tokenService, "secret", otherBase64Key);

        String username = tokenService.validateToken(token);

        assertEquals("", username);
    }

    @Test
    void validateToken_ShouldReturnEmptyString_WhenTokenIsExpired() {
        // Define tempo negativo para gerar token já expirado
        ReflectionTestUtils.setField(tokenService, "expirationTime", -1000L); 
        String expiredToken = tokenService.generateToken(testUser);

        // Garante que a chave está correta para o teste focar apenas na expiração
        ReflectionTestUtils.setField(tokenService, "secret", secretKeyBase64);

        String username = tokenService.validateToken(expiredToken);
        assertEquals("", username);
    }
}