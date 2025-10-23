package com.karaoke.backend.controllers;

import com.karaoke.backend.dtos.AddSongRequestDTO;
import com.karaoke.backend.models.KaraokeSession;
import com.karaoke.backend.services.KaraokeService;
import com.karaoke.backend.services.exception.SessionNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.karaoke.backend.services.TokenService;
import com.karaoke.backend.repositories.UserRepository; 
import org.springframework.security.test.context.support.WithMockUser;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;


import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = KaraokeController.class)
public class KaraokeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private KaraokeService service; 

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TokenService tokenService;
    @MockBean
    private UserRepository userRepository;

    private KaraokeSession testSession;
    private final String SESSION_CODE = "TESTE123";
    private final String BASE_URL = "/api/sessions";

    @BeforeEach
    void setUp() {
        testSession = new KaraokeSession();
        testSession.setAccessCode(SESSION_CODE);
    }

    @Test
    @WithMockUser
    void createSession_ShouldReturn201CreatedAndSession() throws Exception {
        when(service.createSession()).thenReturn(testSession);

        mockMvc.perform(post(BASE_URL)
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", BASE_URL + "/" + SESSION_CODE))
                .andExpect(jsonPath("$.accessCode").value(SESSION_CODE));
        
        verify(service, times(1)).createSession();
    }

    @Test
    @WithMockUser
    void getAllSessions_ShouldReturn200OkAndList() throws Exception {
        KaraokeSession anotherSession = new KaraokeSession();
        anotherSession.setAccessCode("OTHER456");
        List<KaraokeSession> allSessions = Arrays.asList(testSession, anotherSession);

        when(service.getAllSessions()).thenReturn(allSessions);

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].accessCode").value(SESSION_CODE));
        
        verify(service, times(1)).getAllSessions();
    }

    @Test
    @WithMockUser
    void getSession_WithValidCode_ShouldReturn200OkAndSession() throws Exception {
        when(service.getSession(eq(SESSION_CODE))).thenReturn(testSession);

        mockMvc.perform(get(BASE_URL + "/teste123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessCode").value(SESSION_CODE));
        
        verify(service, times(1)).getSession(eq(SESSION_CODE));
    }

    @Test
    @WithMockUser
    void getSession_WithInvalidCode_ShouldReturn404NotFound() throws Exception {
        final String INVALID_CODE = "INVALID404";
        
        when(service.getSession(eq(INVALID_CODE))).thenThrow(new SessionNotFoundException("Sessão não encontrada."));

        mockMvc.perform(get(BASE_URL + "/" + INVALID_CODE))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Sessão não encontrada."));
        
        verify(service, times(1)).getSession(eq(INVALID_CODE));
    }

    @Test
    @WithMockUser 
    void addSongToQueue_ShouldReturn200Ok() throws Exception {
        doNothing().when(service).addSongToQueue(eq(SESSION_CODE), any(), any(), any());

        AddSongRequestDTO requestDTO = new AddSongRequestDTO();
        requestDTO.setYoutubeUrl("youtubeUrlExample");
        requestDTO.setUserId("user123");
        requestDTO.setUserName("João");
        requestDTO.setSongId("song456");

        mockMvc.perform(post(BASE_URL + "/" + SESSION_CODE + "/queue")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO))
                        .with(csrf()))
                .andExpect(status().isOk());
        
        verify(service, times(1)).addSongToQueue(
            eq(SESSION_CODE), 
            eq(requestDTO.getYoutubeUrl()), 
            eq(requestDTO.getUserId()), 
            eq(requestDTO.getUserName())
        );
    }
}

