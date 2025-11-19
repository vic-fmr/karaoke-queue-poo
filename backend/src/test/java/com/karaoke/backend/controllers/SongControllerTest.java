package com.karaoke.backend.controllers;

import com.karaoke.backend.dtos.YouTubeVideoDTO;
import com.karaoke.backend.services.YoutubeService;
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
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get; 
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.hasSize;

@WebMvcTest(controllers = SongController.class, 
    excludeAutoConfiguration = SecurityAutoConfiguration.class, 
    excludeFilters = { 
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class),
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthFilter.class)
    })
class SongControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private YoutubeService youtubeService;

    @Test
    void deveRetornarVideosAoPesquisar() throws Exception {
        // --- ARRANGE (Preparação) ---
        String queryOriginal = "karaoke challenge";
        
        // Criação dos objetos simulados
        YouTubeVideoDTO video1 = new YouTubeVideoDTO("id-video-123", "Melhor Video (Primeiro)", "tbmNail", true);
        YouTubeVideoDTO video2 = new YouTubeVideoDTO("id-video-1234", "Melhor Video (Segundo)", "tbmNail2", false);
        List<YouTubeVideoDTO> mockResult = List.of(video1, video2);

        // CORREÇÃO 1: O Mock deve esperar a string que o controller REALMENTE envia
        // Opção A (Exata):
        when(youtubeService.searchVideos(queryOriginal + "+karaoke")).thenReturn(mockResult);
        
        // Opção B (Genérica - Aceita qualquer string):
        // when(youtubeService.searchVideos(anyString())).thenReturn(mockResult);

        // --- ACT & ASSERT (Ação e Verificação) ---
        mockMvc.perform(
                get("/api/videos/search") 
                    .param("query", queryOriginal) // Envia "karaoke challenge"
                    .contentType(MediaType.APPLICATION_JSON))
                
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2))) 
                
                // CORREÇÃO 2: Os valores esperados devem bater com os objetos criados acima (video1 e video2)
                .andExpect(jsonPath("$[0].title", is("Melhor Video (Primeiro)")))
                .andExpect(jsonPath("$[0].videoId", is("id-video-123"))) 
                .andExpect(jsonPath("$[1].title", is("Melhor Video (Segundo)")));
    }
}