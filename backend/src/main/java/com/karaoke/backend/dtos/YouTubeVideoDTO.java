package com.karaoke.backend.dtos;

public record YouTubeVideoDTO(
    String videoId,         
    String title,            
    String embedUrl,
    String thumbnail,         
    boolean probablyEmbeddable 
) {

    public YouTubeVideoDTO(String videoId, String title, String thumbnail ,boolean probablyEmbeddable) {
        this(videoId, 
             title, 
             "https://www.youtube.com/embed/" + videoId, // Monta a URL de embed aqui
             thumbnail,
             probablyEmbeddable);
    }
}
