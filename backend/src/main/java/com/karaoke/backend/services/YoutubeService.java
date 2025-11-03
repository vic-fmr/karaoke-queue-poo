package com.karaoke.backend.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.karaoke.backend.dtos.YouTubeVideoDTO;

@Service
public class YouTubeService {

    // Chave da API configurada no application.properties
    @Value("${youtube.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private static final String YOUTUBE_API_URL = "https://www.googleapis.com/youtube/v3/";
    
    private static final String USER_REGION_CODE = "BR"; 

    public List<YouTubeVideoDTO> searchVideos(String query) {

        List<Map<String, Object>> searchResults = callSearchList(query);
        
        if (searchResults.isEmpty()) {
            return new ArrayList<>();
        }

        List<String> videoIds = searchResults.stream()
            .map(item -> (String) ((Map<String, Object>) item.get("id")).get("videoId"))
            .collect(Collectors.toList());
            
        Map<String, Boolean> validationMap = checkDetailedRestrictions(videoIds);
        
        List<YouTubeVideoDTO> finalValidList = new ArrayList<>();
        
        for (Map<String, Object> item : searchResults) {
            String videoId = (String) ((Map<String, Object>) item.get("id")).get("videoId");
            String title = (String) ((Map<String, Object>) item.get("snippet")).get("title");
            
            boolean isValid = validationMap.getOrDefault(videoId, false);
            
            if (isValid) {
                YouTubeVideoDTO dto = new YouTubeVideoDTO(videoId, title, true); 
                finalValidList.add(dto);
            }
        }
        
        return finalValidList; 
    }

    private List<Map<String, Object>> callSearchList(String query) {
        String searchUrl = UriComponentsBuilder.fromHttpUrl(YOUTUBE_API_URL + "search")
            .queryParam("key", apiKey)
            .queryParam("part", "snippet")
            .queryParam("q", query)
            .queryParam("type", "video")
            .queryParam("maxResults", 10)
            .queryParam("videoEmbeddable", true)
            .toUriString();

        try {
            Map<String, Object> response = restTemplate.getForObject(searchUrl, Map.class);
            return (List<Map<String, Object>>) response.get("items");
        } catch (Exception e) {
            // Trate a exceção de forma adequada (log, throw custom exception)
            System.err.println("Erro ao buscar no YouTube: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private Map<String, Boolean> checkDetailedRestrictions(List<String> videoIds) {
        String idsString = String.join(",", videoIds);
        
        String detailsUrl = UriComponentsBuilder.fromHttpUrl(YOUTUBE_API_URL + "videos")
            .queryParam("key", apiKey)
            .queryParam("part", "status,contentDetails")
            .queryParam("id", idsString)
            .toUriString();
        
        Map<String, Object> validationMap = new java.util.HashMap<>();
        
        try {
            Map<String, Object> response = restTemplate.getForObject(detailsUrl, Map.class);
            List<Map<String, Object>> detailedItems = (List<Map<String, Object>>) response.get("items");

            for (Map<String, Object> detailedItem : detailedItems) {
                String videoId = (String) detailedItem.get("id");
                
                // 1. Checagem de Permissão Geral
                Map<String, Object> status = (Map<String, Object>) detailedItem.get("status");
                boolean isEmbeddable = (Boolean) status.get("embeddable");

                // 2. Checagem de Restrição Geográfica
                boolean isRegionBlocked = false;
                Map<String, Object> contentDetails = (Map<String, Object>) detailedItem.get("contentDetails");
                
                if (contentDetails.containsKey("regionRestriction")) {
                    Map<String, Object> regionRestriction = (Map<String, Object>) contentDetails.get("regionRestriction");
                    
                    if (regionRestriction.containsKey("blocked")) {
                        List<String> blockedRegions = (List<String>) regionRestriction.get("blocked");
                        if (blockedRegions.contains(USER_REGION_CODE)) {
                            isRegionBlocked = true;
                        }
                    }
                }
                
                validationMap.put(videoId, isEmbeddable && !isRegionBlocked);
            }
        } catch (Exception e) {
            System.err.println("Erro ao checar detalhes do vídeo: " + e.getMessage());
        }

        return (Map<String, Boolean>) (Map) validationMap;
    }
}