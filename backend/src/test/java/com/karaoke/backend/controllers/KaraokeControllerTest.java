package com.karaoke.backend.controllers;

import com.karaoke.backend.dtos.AddSongRequestDTO;
import com.karaoke.backend.exception.SessionNotFoundException;
import com.karaoke.backend.exception.VideoNotFoundException;
import com.karaoke.backend.models.KaraokeSession;
import com.karaoke.backend.models.User;
import com.karaoke.backend.services.KaraokeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Define a classe de teste para o Controller e suas dependências HTTP
@WebMvcTest(KaraokeController.class)
class KaraokeControllerTest {

    @Autowired
    private MockMvc mockMvc; // Usado para simular chamadas HTTP

    @MockBean
    private KaraokeService service; // Cria um Mock do Serviço para isolar o Controller

    private final String ACCESS_CODE = "TEST1A";
    private KaraokeSession mockSession;
    private User mockUser;

    @BeforeEach
    void setUp() {
        mockSession = new KaraokeSession();
        mockSession.setAccessCode(ACCESS_CODE);
        mockSession.setId(1L);

        mockUser = new User();
        mockUser.setId(99L);
        // Nota: O @AuthenticationPrincipal injeta um mockUser quando @WithMockUser é usado
    }

    // -----------------------------------------------------------------------------------
    // Testes para POST /api/sessions (createSession)
    // -----------------------------------------------------------------------------------

    @Test
    void createSession_DeveRetornar201CreatedComHeaderLocation() throws Exception {
        // Arrange
        when(service.createSession()).thenReturn(mockSession);

        // Act & Assert
        mockMvc.perform(post("/api/sessions")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated()) // Espera status 201
                .andExpect(header().string("Location", "/api/sessions/" + ACCESS_CODE)) // Espera o Header Location
                .andExpect(jsonPath("$.accessCode").value(ACCESS_CODE)); // Verifica o corpo retornado

        verify(service, times(1)).createSession();
    }

    // -----------------------------------------------------------------------------------
    // Testes para GET /api/sessions (getAllSessions)
    // -----------------------------------------------------------------------------------

    @Test
    void getAllSessions_DeveRetornar200OkComListaDeSessoes() throws Exception {
        // Arrange
        List<KaraokeSession> sessions = List.of(mockSession, new KaraokeSession());
        when(service.getAllSessions()).thenReturn(sessions);

        // Act & Assert
        mockMvc.perform(get("/api/sessions")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()) // Espera status 200
                .andExpect(jsonPath("$.length()").value(2)); // Verifica o tamanho da lista

        verify(service, times(1)).getAllSessions();
    }

    // -----------------------------------------------------------------------------------
    // Testes para GET /api/sessions/{sessionCode} (getSession)
    // -----------------------------------------------------------------------------------

    @Test
    void getSession_DeveRetornar200Ok_QuandoSessaoEncontrada() throws Exception {
        // Arrange
        when(service.getSession(ACCESS_CODE)).thenReturn(mockSession);

        // Act & Assert
        mockMvc.perform(get("/api/sessions/" + ACCESS_CODE)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()) // Espera status 200
                .andExpect(jsonPath("$.accessCode").value(ACCESS_CODE));

        verify(service, times(1)).getSession(ACCESS_CODE);
    }

    @Test
    void getSession_DeveRetornar404NotFound_QuandoSessaoNaoEncontrada() throws Exception {
        // Arrange
        when(service.getSession(ACCESS_CODE)).thenThrow(new SessionNotFoundException("Sessão não existe."));

        // Act & Assert
        // Se a exceção SessionNotFoundException não for tratada por um @ControllerAdvice,
        // o Spring geralmente retorna 500. No entanto, em um ambiente real, você esperaria
        // um 404 (Not Found) para exceções de "não encontrado".
        // Assumindo que você tem um tratamento para 404 no seu projeto:
        // Se não tiver tratamento, mude para .andExpect(status().isInternalServerError()) ou
        // implemente o tratamento de exceções. Vamos manter o 404 como ideal.
        mockMvc.perform(get("/api/sessions/" + ACCESS_CODE))
                .andExpect(status().isNotFound()); // Idealmente, 404 para não encontrado

        verify(service, times(1)).getSession(ACCESS_CODE);
    }

    // -----------------------------------------------------------------------------------
    // Testes para POST /api/sessions/{sessionCode}/queue (addSongToQueue)
    // -----------------------------------------------------------------------------------

    @Test
    @WithMockUser(username = "testUser") // Simula um usuário autenticado
    void addSongToQueue_DeveRetornar201Created_QuandoAdicionadoComSucesso() throws Exception {
        // Arrange
        String songTitle = "Bohemian Rhapsody";
        AddSongRequestDTO requestDTO = new AddSongRequestDTO(songTitle);

        // Mock do corpo da requisição
        String requestBody = "{\"songTitle\": \"" + songTitle + "\"}";

        // Não precisamos simular o retorno, apenas que o serviço é chamado
        doNothing().when(service).addSongToQueue(eq(ACCESS_CODE), eq(songTitle), any(User.class));

        // Act & Assert
        mockMvc.perform(post("/api/sessions/" + ACCESS_CODE + "/queue")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated()); // Espera status 201

        // Verifica se o serviço foi chamado com os argumentos corretos
        verify(service, times(1)).addSongToQueue(
                eq(ACCESS_CODE),
                eq(songTitle),
                any(User.class) // O MockMvc injeta um objeto User mockado
        );
    }

    @Test
    @WithMockUser(username = "testUser")
    void addSongToQueue_DeveRetornar404NotFound_QuandoSessaoNaoExiste() throws Exception {
        // Arrange
        String songTitle = "Some Song";
        String requestBody = "{\"songTitle\": \"" + songTitle + "\"}";

        // Simula a exceção lançada pelo serviço
        doThrow(new SessionNotFoundException("Sessão inexistente.")).when(service)
                .addSongToQueue(eq(ACCESS_CODE), eq(songTitle), any(User.class));

        // Act & Assert
        mockMvc.perform(post("/api/sessions/" + ACCESS_CODE + "/queue")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isNotFound()); // Idealmente, 404
    }

    @Test
    @WithMockUser(username = "testUser")
    void addSongToQueue_DeveRetornar404NotFound_QuandoVideoNaoEncontrado() throws Exception {
        // Arrange
        String songTitle = "Bad Song";
        String requestBody = "{\"songTitle\": \"" + songTitle + "\"}";

        // Simula a exceção VideoNotFoundException
        doThrow(new VideoNotFoundException("Vídeo não encontrado.")).when(service)
                .addSongToQueue(eq(ACCESS_CODE), eq(songTitle), any(User.class));

        // Act & Assert
        mockMvc.perform(post("/api/sessions/" + ACCESS_CODE + "/queue")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isNotFound()); // Um 404 ou 400 (Bad Request) seria apropriado
    }

    // -----------------------------------------------------------------------------------
    // Testes para DELETE /api/sessions/{sessionCode} (endSession)
    // -----------------------------------------------------------------------------------

    @Test
    void endSession_DeveRetornar204NoContent_QuandoFinalizadaComSucesso() throws Exception {
        // Arrange
        doNothing().when(service).endSession(ACCESS_CODE);

        // Act & Assert
        mockMvc.perform(delete("/api/sessions/" + ACCESS_CODE))
                .andExpect(status().isNoContent()); // Espera status 204

        verify(service, times(1)).endSession(ACCESS_CODE);
    }

    // -----------------------------------------------------------------------------------
    // Testes para DELETE /api/sessions/{sessionCode}/queue/{queueItemId} (deleteSongFromQueue)
    // -----------------------------------------------------------------------------------

    @Test
    void deleteSongFromQueue_DeveRetornar204NoContent() throws Exception {
        // Arrange
        Long queueItemId = 123L;
        doNothing().when(service).deleteSongFromQueue(ACCESS_CODE, queueItemId);

        // Act & Assert
        mockMvc.perform(delete("/api/sessions/" + ACCESS_CODE + "/queue/" + queueItemId))
                .andExpect(status().isNoContent()); // Espera status 204

        verify(service, times(1)).deleteSongFromQueue(ACCESS_CODE, queueItemId);
    }
}