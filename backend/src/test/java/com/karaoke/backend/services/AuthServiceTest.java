package com.karaoke.backend.services;

import com.karaoke.backend.dtos.RegisterRequestDTO;
import com.karaoke.backend.exception.UserAlreadyExistsException;
import com.karaoke.backend.models.User;
import com.karaoke.backend.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    // Dados de teste
    private final String EMAIL = "teste@email.com";
    private final String USERNAME = "testUser";
    private final String RAW_PASSWORD = "rawPassword";
    private final String ENCODED_PASSWORD = "encodedPassword123";
    private RegisterRequestDTO registerDTO;
    private User mockUser;

    @BeforeEach
    void setUp() {
        // Inicializa o DTO de registro
        registerDTO = new RegisterRequestDTO(USERNAME, EMAIL, RAW_PASSWORD);

        // Inicializa um mock de User
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername(USERNAME);
        mockUser.setEmail(EMAIL);
        mockUser.setPassword(ENCODED_PASSWORD);
    }

    // -----------------------------------------------------------------------------------
    // Testes para register(RegisterRequestDTO dto)
    // -----------------------------------------------------------------------------------

    @Test
    void register_DeveCriarNovoUsuario_QuandoEmailNaoExiste() {
        // Arrange
        // 1. Simula que o e-mail não existe no repositório
        when(userRepository.existsByEmail(EMAIL)).thenReturn(false);
        // 2. Simula a codificação da senha
        when(passwordEncoder.encode(RAW_PASSWORD)).thenReturn(ENCODED_PASSWORD);
        // 3. Simula o salvamento do usuário, retornando o usuário mockado com ID
        when(userRepository.save(any(User.class))).thenReturn(mockUser);

        // Act
        User result = authService.register(registerDTO);

        // Assert
        assertNotNull(result);
        assertEquals(EMAIL, result.getEmail());
        assertEquals(USERNAME, result.getUsername());
        // Verifica se a senha salva é a ENCODED_PASSWORD
        assertEquals(ENCODED_PASSWORD, result.getPassword());

        // Verifica se os métodos foram chamados corretamente
        verify(userRepository, times(1)).existsByEmail(EMAIL);
        verify(passwordEncoder, times(1)).encode(RAW_PASSWORD);
        // Captura o argumento passado para save para verificar se os dados estão corretos ANTES de salvar
        verify(userRepository, times(1)).save(argThat(user ->
                user.getEmail().equals(EMAIL) &&
                        user.getUsername().equals(USERNAME) &&
                        user.getPassword().equals(ENCODED_PASSWORD)
        ));
    }

    @Test
    void register_DeveLancarUserAlreadyExistsException_QuandoEmailJaExiste() {
        // Arrange
        // Simula que o e-mail já existe no repositório
        when(userRepository.existsByEmail(EMAIL)).thenReturn(true);

        // Act & Assert
        // Verifica se a exceção correta é lançada
        assertThrows(UserAlreadyExistsException.class,
                () -> authService.register(registerDTO));

        // Verifica se o método de checagem foi chamado
        verify(userRepository, times(1)).existsByEmail(EMAIL);
        // Verifica que a codificação da senha e o salvamento NUNCA ocorreram
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    // -----------------------------------------------------------------------------------
    // Testes para loadUserByUsername(String email)
    // -----------------------------------------------------------------------------------

    @Test
    void loadUserByUsername_DeveRetornarUserDetails_QuandoUsuarioEncontrado() {
        // Arrange
        // Simula a busca bem-sucedida pelo e-mail
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(mockUser));

        // Act
        UserDetails result = authService.loadUserByUsername(EMAIL);

        // Assert
        assertNotNull(result);
        
        // CORREÇÃO AQUI:
        // O método getUsername() da interface UserDetails retorna o campo "username" do objeto User ("testUser"),
        // e não o email, mesmo que a busca tenha sido feita por email.
        assertEquals(USERNAME, result.getUsername()); 
        
        // Se você quisesse validar o email, teria que fazer o cast, pois UserDetails não tem getEmail():
        // assertEquals(EMAIL, ((User) result).getEmail());

        assertInstanceOf(User.class, result);

        verify(userRepository, times(1)).findByEmail(EMAIL);
    }

    @Test
    void loadUserByUsername_DeveLancarUsernameNotFoundException_QuandoUsuarioNaoEncontrado() {
        // Arrange
        // Simula que o usuário não foi encontrado
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        // Act & Assert
        // Verifica se a exceção correta é lançada
        assertThrows(UsernameNotFoundException.class,
                () -> authService.loadUserByUsername(EMAIL));

        verify(userRepository, times(1)).findByEmail(EMAIL);
    }
}