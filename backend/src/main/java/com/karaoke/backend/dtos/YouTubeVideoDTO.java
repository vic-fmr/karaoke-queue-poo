package com.karaoke.backend.dtos;

public record YouTubeVideoDTO(
    String videoId,         
    String title,            
    String embedUrl,         
    boolean probablyEmbeddable 
) {

    public YouTubeVideoDTO(String videoId, String title, boolean probablyEmbeddable) {
        this(videoId, 
             title, 
             "https://www.youtube.com/embed/" + videoId, // Monta a URL de embed aqui
             probablyEmbeddable);
    }
}
