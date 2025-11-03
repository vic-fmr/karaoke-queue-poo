package com.karaoke.backend.services;

import com.karaoke.backend.dtos.YouTubeVideoDTO;
import com.karaoke.backend.services.YoutubeService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class YouTubeServiceTest {

    @InjectMocks
    private YoutubeService youTubeService;

    @Mock
    private RestTemplate restTemplate;

    private final String MOCK_API_KEY = "TEST_API_KEY";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(youTubeService, "apiKey", MOCK_API_KEY);
        ReflectionTestUtils.setField(youTubeService, "restTemplate", restTemplate);
    }

    @Test
    void searchVideos_shouldReturnEmptyList_whenNoResultsFound() {
        Map<String, Object> emptySearchResponse = Map.of("items", Collections.emptyList());
        when(restTemplate.getForObject(anyString(), eq(Map.class)))
                .thenReturn(emptySearchResponse);

        List<YouTubeVideoDTO> result = youTubeService.searchVideos("query sem resultados");

        assertTrue(result.isEmpty(), "A lista deve estar vazia quando não houver resultados.");
    }

    @Test
    void searchVideos_shouldReturnValidVideos_whenAllChecksPass() {
        // ARRANGE: Configura um vídeo que é 'embeddable' e sem restrição geográfica

        // 1. Simula a resposta da busca (search.list)
        Map<String, Object> searchItem = Map.of(
                "id", Map.of("videoId", "VIDEO_A"),
                "snippet", Map.of("title", "Música Legal A")
        );
        Map<String, Object> searchResponse = Map.of("items", List.of(searchItem));

        // 2. Simula a resposta de detalhes (videos.list) - O filtro Grosso
        Map<String, Object> detailItem = Map.of(
                "id", "VIDEO_A",
                "status", Map.of("embeddable", true), // Permissão Geral OK
                "contentDetails", Collections.emptyMap() // Sem restrições geográficas
        );
        Map<String, Object> detailResponse = Map.of("items", List.of(detailItem));

        // Define o comportamento do RestTemplate:
        // Primeira chamada (search.list)
        when(restTemplate.getForObject(any(String.class), eq(Map.class)))
                .thenReturn(searchResponse)
                // Segunda chamada (videos.list)
                .thenReturn(detailResponse);

        // ACT
        List<YouTubeVideoDTO> result = youTubeService.searchVideos("Música Legal");

        // ASSERT
        assertFalse(result.isEmpty(), "Deve retornar vídeos válidos.");
        assertEquals(1, result.size());
        YouTubeVideoDTO dto = result.get(0);
        assertEquals("VIDEO_A", dto.videoId());
        assertTrue(dto.probablyEmbeddable(), "A flag probablyEmbeddable deve ser TRUE.");
    }

    // --- Testes de Casos de Falha/Filtro ---

    @Test
    void searchVideos_shouldFilterOut_whenVideoIsNotGenerallyEmbeddable() {
        // ARRANGE: Configura um vídeo que é retornado pela busca, mas não é embeddable

        // 1. Simula a resposta da busca (search.list)
        Map<String, Object> searchItem = Map.of(
                "id", Map.of("videoId", "VIDEO_B"),
                "snippet", Map.of("title", "Música Não Embeddable")
        );
        Map<String, Object> searchResponse = Map.of("items", List.of(searchItem));

        // 2. Simula a resposta de detalhes (videos.list) - status.embeddable = false
        Map<String, Object> detailItem = Map.of(
                "id", "VIDEO_B",
                "status", Map.of("embeddable", false), // FALHA AQUI
                "contentDetails", Collections.emptyMap() 
        );
        Map<String, Object> detailResponse = Map.of("items", List.of(detailItem));

        // Define o comportamento do RestTemplate
        when(restTemplate.getForObject(any(String.class), eq(Map.class)))
                .thenReturn(searchResponse)
                .thenReturn(detailResponse);

        // ACT
        List<YouTubeVideoDTO> result = youTubeService.searchVideos("query");

        // ASSERT
        assertTrue(result.isEmpty(), "O vídeo deve ser filtrado porque não é embeddable.");
    }
    
    @Test
    void searchVideos_shouldFilterOut_whenVideoIsRegionBlocked() {
        // ARRANGE: Configura um vídeo que é bloqueado para a região 'BR' (USER_REGION_CODE)

        // 1. Simula a resposta da busca (search.list)
        Map<String, Object> searchItem = Map.of(
                "id", Map.of("videoId", "VIDEO_C"),
                "snippet", Map.of("title", "Música Bloqueada")
        );
        Map<String, Object> searchResponse = Map.of("items", List.of(searchItem));

        // 2. Simula a resposta de detalhes (videos.list) - Restrição de Região
        Map<String, Object> detailItem = Map.of(
                "id", "VIDEO_C",
                "status", Map.of("embeddable", true),
                "contentDetails", Map.of(
                    "regionRestriction", Map.of(
                        "blocked", List.of("BR", "AR", "CL") // BR está na lista de bloqueio
                    )
                )
        );
        Map<String, Object> detailResponse = Map.of("items", List.of(detailItem));

        // Define o comportamento do RestTemplate
        when(restTemplate.getForObject(any(String.class), eq(Map.class)))
                .thenReturn(searchResponse)
                .thenReturn(detailResponse);

        // ACT
        List<YouTubeVideoDTO> result = youTubeService.searchVideos("query");

        // ASSERT
        assertTrue(result.isEmpty(), "O vídeo deve ser filtrado por estar bloqueado para a região BR.");
    }
}