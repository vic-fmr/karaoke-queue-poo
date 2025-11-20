package com.karaoke.backend.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.karaoke.backend.dtos.LoginRequestDTO;
import com.karaoke.backend.dtos.RegisterRequestDTO;
import com.karaoke.backend.models.User;
import com.karaoke.backend.services.AuthService;
import com.karaoke.backend.services.TokenService;
import com.karaoke.backend.config.SecurityConfig; 
import com.karaoke.backend.config.JwtAuthFilter; 

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthController.class, 
    excludeAutoConfiguration = SecurityAutoConfiguration.class, 
    excludeFilters = { 
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class),
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthFilter.class)
    })
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService; 

    @MockBean
    private TokenService tokenService; 

    @MockBean
    private AuthenticationManager authenticationManager; 

    @Test
    void deveRegistrarComSucesso() throws Exception {
        // --- ARRANGE ---
        RegisterRequestDTO requestDTO = new RegisterRequestDTO("Nome Teste", "teste@email.com", "senha123");
        
        User usuarioCriado = new User();
        usuarioCriado.setId(1L);
        usuarioCriado.setUsername(requestDTO.username()); 
        usuarioCriado.setEmail(requestDTO.email());

        // 1. Mock do Registro (Você já tinha)
        when(authService.register(any(RegisterRequestDTO.class))).thenReturn(usuarioCriado);

        // --- CORREÇÃO 1: Mocks de Autenticação (Obrigatórios pois o controller tenta logar) ---
        Authentication authMock = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authMock);
        
        // Quando o controller pedir o usuário autenticado, retorna o usuário criado
        when(authMock.getPrincipal()).thenReturn(usuarioCriado);
        
        // Mock da Geração do Token
        String tokenGerado = "token_jwt_criado_no_registro";
        when(tokenService.generateToken(any(User.class))).thenReturn(tokenGerado);


        // --- ACT & ASSERT ---
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                
                .andExpect(status().isCreated()) 
                
                // --- CORREÇÃO 2: O retorno é o Token, não o User ---
                // O seu controller retorna: new AuthResponseDTO(token)
                // Então não adianta procurar por "id" ou "email" aqui
                .andExpect(jsonPath("$.token").value(tokenGerado));
    }

    @Test
    void deveLogarComSucesso() throws Exception {
        // (Seu código de login estava correto, mantendo aqui para contexto)
        LoginRequestDTO requestDTO = new LoginRequestDTO("teste@email.com", "senha123");
        
        String tokenJwt = "meu.token.jwt.simulado";
        User usuarioLogado = new User(); 
        usuarioLogado.setEmail(requestDTO.email());

        Authentication authMock = mock(Authentication.class);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authMock);
        
        when(authMock.getPrincipal()).thenReturn(usuarioLogado);
        
        when(tokenService.generateToken(usuarioLogado)).thenReturn(tokenJwt);

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk()) 
                .andExpect(jsonPath("$.token").value(tokenJwt));
    }
}