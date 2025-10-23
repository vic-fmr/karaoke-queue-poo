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
        Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
        secretKeyBase64 = Encoders.BASE64.encode(key.getEncoded());

        ReflectionTestUtils.setField(tokenService, "secret", secretKeyBase64);
        
        testUser = new User();
        testUser.setUsername("testuser");
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

        String username = tokenService.validateToken(token);

        assertEquals("testuser", username);
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

        Key otherKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);
        String otherBase64Key = Encoders.BASE64.encode(otherKey.getEncoded());
        ReflectionTestUtils.setField(tokenService, "secret", otherBase64Key);

        String username = tokenService.validateToken(token);

        assertEquals("", username);
    }

    @Test
    void validateToken_ShouldReturnEmptyString_WhenTokenIsExpired() {
        ReflectionTestUtils.setField(tokenService, "expirationTime", -1L); 
        String expiredToken = tokenService.generateToken(testUser);

        ReflectionTestUtils.setField(tokenService, "secret", secretKeyBase64);

        String username = tokenService.validateToken(expiredToken);
        assertEquals("", username);
    }
}
