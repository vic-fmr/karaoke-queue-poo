package com.karaoke.backend.controllers;

import com.karaoke.backend.dtos.YouTubeVideoDTO;
import com.karaoke.backend.services.YoutubeService;

// Nossas classes de exclusão de segurança
import com.karaoke.backend.config.SecurityConfig; 
import com.karaoke.backend.config.JwtAuthFilter; 

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
// Imports para a exclusão de segurança
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get; 
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.is;

// 1. APLICAMOS EXATAMENTE A MESMA SOLUÇÃO DE SEGURANÇA
@WebMvcTest(controllers = YouTubeController.class, 
    // Exclui a segurança PADRÃO (HttpBasic, CSRF, etc.)
    excludeAutoConfiguration = SecurityAutoConfiguration.class, 
    // Exclui a segurança PERSONALIZADA (para o contexto carregar)
    excludeFilters = { 
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class),
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthFilter.class)
    })
class YouTubeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean // 2. Mocamos a única dependência
    private YoutubeService youtubeService;

    // --- Teste 1: O "Caminho Feliz" (Happy Path) ---
    @Test
    void deveRetornarMelhorVideoQuandoEncontrado() throws Exception {
        // --- ARRANGE (Preparação) ---
        String query = "melhor musica";

        // 3. Criamos uma lista de resultados. O controller só deve retornar o PRIMEIRO.
        YouTubeVideoDTO video1 = new YouTubeVideoDTO("id-video-123", "Melhor Video (Primeiro)", " tbmNail",true);
        YouTubeVideoDTO video2 = new YouTubeVideoDTO("id-video-456", "Outro Video (Segundo)", " tbmNail",false);
        List<YouTubeVideoDTO> mockResult = List.of(video1, video2);

        // 4. Ensinamos o mock
        when(youtubeService.searchVideos(query)).thenReturn(mockResult);

        // --- ACT & ASSERT (Ação e Verificação) ---
        mockMvc.perform(
                get("/api/resolve-video") // 5. Chamamos o endpoint correto
                    .param("query", query)
                    .contentType(MediaType.APPLICATION_JSON))
                
                // 6. Verificações
                .andExpect(status().isOk()) // Espera 200 OK
                // A resposta DEVE ser o video1, e NÃO o video2
                .andExpect(jsonPath("$.videoId", is("id-video-123"))) 
                .andExpect(jsonPath("$.title", is("Melhor Video (Primeiro)")));
    }

    // --- Teste 2: O "Caminho Triste" (Sad Path) ---
    @Test
    void deveRetornarNotFoundQuandoNaoEncontrarVideo() throws Exception {
        // --- ARRANGE (Preparação) ---
        String query = "query que nao retorna nada";

        // 3. Criamos uma lista VAZIA
        List<YouTubeVideoDTO> emptyList = Collections.emptyList();

        // 4. Ensinamos o mock a retornar a lista vazia
        when(youtubeService.searchVideos(query)).thenReturn(emptyList);

        // --- ACT & ASSERT (Ação e Verificação) ---
        mockMvc.perform(
                get("/api/resolve-video")
                    .param("query", query)
                    .contentType(MediaType.APPLICATION_JSON))
                
                // 6. Verificamos se o status é 404 Not Found
                .andExpect(status().isNotFound());
    }
}