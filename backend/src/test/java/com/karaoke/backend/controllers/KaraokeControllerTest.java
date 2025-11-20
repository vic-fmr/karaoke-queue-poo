package com.karaoke.backend.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.karaoke.backend.config.JwtAuthFilter;
import com.karaoke.backend.config.SecurityConfig;
import com.karaoke.backend.dtos.AddSongRequestDTO;
import com.karaoke.backend.dtos.YouTubeVideoDTO;
import com.karaoke.backend.exception.SessionNotFoundException;
import com.karaoke.backend.exception.VideoNotFoundException;
import com.karaoke.backend.models.KaraokeSession;
import com.karaoke.backend.models.User;
import com.karaoke.backend.services.KaraokeService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// 1. CORREÇÃO PRINCIPAL: Adicionar as exclusões de segurança igual ao AuthController
@WebMvcTest(controllers = KaraokeController.class,
    excludeAutoConfiguration = SecurityAutoConfiguration.class,
    excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class),
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthFilter.class)
    })
class KaraokeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // 2. Adicionar ObjectMapper para gerar o JSON automaticamente e evitar erros de string manual
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private KaraokeService service;

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
    }

    // -----------------------------------------------------------------------------------
    // Testes POST /api/sessions
    // -----------------------------------------------------------------------------------
    @Test
    @WithMockUser // Simula usuário para passar pelo contexto (mesmo sem filtros, é boa prática)
    void createSession_DeveRetornar201CreatedComHeaderLocation() throws Exception {
        when(service.createSession()).thenReturn(mockSession);

        mockMvc.perform(post("/api/sessions")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/sessions/" + ACCESS_CODE))
                .andExpect(jsonPath("$.accessCode").value(ACCESS_CODE));

        verify(service, times(1)).createSession();
    }

    // -----------------------------------------------------------------------------------
    // Testes GET /api/sessions
    // -----------------------------------------------------------------------------------
    @Test
    @WithMockUser
    void getAllSessions_DeveRetornar200OkComListaDeSessoes() throws Exception {
        List<KaraokeSession> sessions = List.of(mockSession, new KaraokeSession());
        when(service.getAllSessions()).thenReturn(sessions);

        mockMvc.perform(get("/api/sessions")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        verify(service, times(1)).getAllSessions();
    }

    // -----------------------------------------------------------------------------------
    // Testes GET /api/sessions/{sessionCode}
    // -----------------------------------------------------------------------------------
    @Test
    @WithMockUser
    void getSession_DeveRetornar200Ok_QuandoSessaoEncontrada() throws Exception {
        when(service.getSession(ACCESS_CODE)).thenReturn(mockSession);

        mockMvc.perform(get("/api/sessions/" + ACCESS_CODE)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessCode").value(ACCESS_CODE));

        verify(service, times(1)).getSession(ACCESS_CODE);
    }

    @Test
    @WithMockUser
    void getSession_DeveRetornar404NotFound_QuandoSessaoNaoEncontrada() throws Exception {
        when(service.getSession(ACCESS_CODE)).thenThrow(new SessionNotFoundException("Sessão não existe."));

        mockMvc.perform(get("/api/sessions/" + ACCESS_CODE))
                .andExpect(status().isNotFound());

        verify(service, times(1)).getSession(ACCESS_CODE);
    }

    // -----------------------------------------------------------------------------------
    // Testes POST /api/sessions/{sessionCode}/queue
    // -----------------------------------------------------------------------------------
    @Test
    @WithMockUser(username = "testUser") 
    void addSongToQueue_DeveRetornar201Created_QuandoAdicionadoComSucesso() throws Exception {
        // ARRANGE
        // 3. CORREÇÃO: Usar o DTO correto que o Controller espera no @RequestBody
        AddSongRequestDTO requestDTO = new AddSongRequestDTO(
            "VIDEO_ID_123", 
            "Bohemian Rhapsody (Cover)", 
            "http://thumbnail.url/img.jpg"
        );

        // Configura o Mock: O serviço recebe um YouTubeVideoDTO (convertido pelo controller), não o AddSongRequestDTO
        doNothing().when(service).addSongToQueue(
                eq(ACCESS_CODE), 
                any(YouTubeVideoDTO.class), 
                any(User.class) // O User vem do @AuthenticationPrincipal (simulado pelo @WithMockUser)
        );

        // ACT & ASSERT
        mockMvc.perform(post("/api/sessions/" + ACCESS_CODE + "/queue")
                        .contentType(MediaType.APPLICATION_JSON)
                        // Usa o ObjectMapper para gerar o JSON correto do AddSongRequestDTO
                        .content(objectMapper.writeValueAsString(requestDTO))) 
                .andExpect(status().isCreated());

        verify(service, times(1)).addSongToQueue(
                eq(ACCESS_CODE),
                any(YouTubeVideoDTO.class),
                any(User.class)
        );
    }

    @Test
    @WithMockUser(username = "testUser")
    void addSongToQueue_DeveRetornar404NotFound_QuandoSessaoNaoExiste() throws Exception {
        // ARRANGE
        AddSongRequestDTO requestDTO = new AddSongRequestDTO("V1", "Test Song", "url");

        doThrow(new SessionNotFoundException("Sessão inexistente.")).when(service)
                .addSongToQueue(eq(ACCESS_CODE), any(YouTubeVideoDTO.class), any(User.class));

        // ACT & ASSERT
        mockMvc.perform(post("/api/sessions/" + ACCESS_CODE + "/queue")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "testUser")
    void addSongToQueue_DeveRetornar404NotFound_QuandoVideoNaoEncontrado() throws Exception {
        // ARRANGE
        AddSongRequestDTO requestDTO = new AddSongRequestDTO("V_ERR", "Missing Video", "url");

        doThrow(new VideoNotFoundException("Vídeo não encontrado.")).when(service)
                .addSongToQueue(eq(ACCESS_CODE), any(YouTubeVideoDTO.class), any(User.class));

        // ACT & ASSERT
        mockMvc.perform(post("/api/sessions/" + ACCESS_CODE + "/queue")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isNotFound());
    }

    // -----------------------------------------------------------------------------------
    // Testes DELETE /api/sessions/{sessionCode}
    // -----------------------------------------------------------------------------------
    @Test
    @WithMockUser // Delete geralmente requer permissão
    void endSession_DeveRetornar204NoContent_QuandoFinalizadaComSucesso() throws Exception {
        doNothing().when(service).endSession(ACCESS_CODE);

        mockMvc.perform(delete("/api/sessions/" + ACCESS_CODE))
                .andExpect(status().isNoContent());

        verify(service, times(1)).endSession(ACCESS_CODE);
    }

    // -----------------------------------------------------------------------------------
    // Testes DELETE /api/sessions/{sessionCode}/queue/{queueItemId}
    // -----------------------------------------------------------------------------------
    @Test
    @WithMockUser
    void deleteSongFromQueue_DeveRetornar204NoContent() throws Exception {
        Long queueItemId = 123L;
        doNothing().when(service).deleteSongFromQueue(ACCESS_CODE, queueItemId);

        mockMvc.perform(delete("/api/sessions/" + ACCESS_CODE + "/queue/" + queueItemId))
                .andExpect(status().isNoContent());

        verify(service, times(1)).deleteSongFromQueue(ACCESS_CODE, queueItemId);
    }
}