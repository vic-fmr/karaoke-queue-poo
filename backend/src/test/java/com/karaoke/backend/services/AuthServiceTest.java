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
    void register_ShouldSaveNewUser_WhenUsernameIsAvailable() {
        RegisterRequestDTO dto = new RegisterRequestDTO("testuser", "test@email.com", "password123");
        User newUser = new User();
        newUser.setUsername(dto.username());
        newUser.setPassword("encodedPassword");

        when(userRepository.existsByUsername(dto.username())).thenReturn(false);
        when(passwordEncoder.encode(dto.password())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(newUser);

        User savedUser = authService.register(dto);

        assertNotNull(savedUser);
        assertEquals("testuser", savedUser.getUsername());
        assertEquals("encodedPassword", savedUser.getPassword());

        verify(userRepository, times(1)).existsByUsername("testuser");
        verify(passwordEncoder, times(1)).encode("password123");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void register_ShouldThrowException_WhenUsernameExists() {
        RegisterRequestDTO dto = new RegisterRequestDTO("existinguser", "test@email.com", "password123");
        when(userRepository.existsByUsername("existinguser")).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.register(dto);
        });

        assertEquals("Usuário já existe", exception.getMessage());

        verify(userRepository, never()).save(any(User.class));
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void loadUserByUsername_ShouldReturnUserDetails_WhenUserExists() {
        User mockUser = new User();
        mockUser.setUsername("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(mockUser));

        UserDetails userDetails = authService.loadUserByUsername("testuser");

        assertNotNull(userDetails);
        assertEquals("testuser", userDetails.getUsername());
        verify(userRepository, times(1)).findByUsername("testuser");
    }

    @Test
    void loadUserByUsername_ShouldThrowException_WhenUserDoesNotExist() {
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> {
            authService.loadUserByUsername("nonexistent");
        });

        assertEquals("Usuário não encontrado: nonexistent", exception.getMessage());
        verify(userRepository, times(1)).findByUsername("nonexistent");
    }
}
