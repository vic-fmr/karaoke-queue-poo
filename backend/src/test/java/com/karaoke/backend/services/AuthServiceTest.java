package com.karaoke.backend.services;

import com.karaoke.backend.dtos.RegisterRequestDTO;
import com.karaoke.backend.models.User;
import com.karaoke.backend.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @Test
    // Nome do teste atualizado para refletir a lógica de e-mail
    void register_ShouldSaveNewUser_WhenEmailIsAvailable() { 
        RegisterRequestDTO dto = new RegisterRequestDTO("testuser", "test@email.com", "password123");
        User newUser = new User();
        newUser.setUsername(dto.username());
        newUser.setEmail(dto.email()); // Adicionado para um mock mais completo
        newUser.setPassword("encodedPassword");

        // <-- MUDANÇA: Verificar se existe por E-MAIL, não por username
        when(userRepository.existsByEmail(dto.email())).thenReturn(false);
        when(passwordEncoder.encode(dto.password())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(newUser);

        User savedUser = authService.register(dto);

        assertNotNull(savedUser);
        assertEquals("testuser", savedUser.getUsername());
        assertEquals("test@email.com", savedUser.getEmail()); // <-- NOVA ASSERTIVA: Verificar o e-mail
        assertEquals("encodedPassword", savedUser.getPassword());

        // <-- MUDANÇA: Verificar a chamada do método de e-mail
        verify(userRepository, times(1)).existsByEmail("test@email.com");
        verify(passwordEncoder, times(1)).encode("password123");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    // Nome do teste atualizado
    void register_ShouldThrowException_WhenEmailExists() {
        RegisterRequestDTO dto = new RegisterRequestDTO("existinguser", "test@email.com", "password123");
        
        // <-- MUDANÇA: Mock para retornar true na verificação de E-MAIL
        when(userRepository.existsByEmail("test@email.com")).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.register(dto);
        });

        assertEquals("Usuário já existe", exception.getMessage());

        // <-- MUDANÇA: Verificar se o 'existsByEmail' foi chamado
        verify(userRepository, times(1)).existsByEmail("test@email.com");
        verify(userRepository, never()).save(any(User.class));
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void loadUserByUsername_ShouldReturnUserDetails_WhenUserExists() {
        String testEmail = "test@email.com";
        User mockUser = new User();
        mockUser.setUsername("testuser");
        mockUser.setEmail(testEmail);

        // <-- MUDANÇA: O método agora busca por e-mail
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(mockUser));

        // <-- MUDANÇA: Passar o e-mail como parâmetro
        UserDetails userDetails = authService.loadUserByUsername(testEmail);

        assertNotNull(userDetails);
        // O UserDetails retornado ainda deve ter o username correto
        assertEquals("testuser", userDetails.getUsername()); 
        
        // <-- MUDANÇA: Verificar se 'findByEmail' foi chamado
        verify(userRepository, times(1)).findByEmail(testEmail);
    }

    @Test
    void loadUserByUsername_ShouldThrowException_WhenUserDoesNotExist() {
        String testEmail = "nonexistent@email.com";
        
        // <-- MUDANÇA: Mock da busca por e-mail
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.empty());

        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> {
            // <-- MUDANÇA: Passar o e-mail
            authService.loadUserByUsername(testEmail);
        });

        // <-- MUDANÇA: A mensagem de exceção agora deve conter o e-mail
        assertEquals("Usuário não encontrado: " + testEmail, exception.getMessage());
        
        // <-- MUDANÇA: Verificar se 'findByEmail' foi chamado
        verify(userRepository, times(1)).findByEmail(testEmail);
    }
}