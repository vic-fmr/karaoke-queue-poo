package com.karaoke.backend.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class YouTubeVideoDTO {

    private String videoId;
    private String title;
    private String embedUrl;
    private String thumbnail;
    private boolean probablyEmbeddable;

    public YouTubeVideoDTO(String videoId, String title, String thumbnail, boolean probablyEmbeddable) {
        this.videoId = videoId;
        this.title = title;
        this.thumbnail = thumbnail;
        this.probablyEmbeddable = probablyEmbeddable;
        this.embedUrl = "https://www.youtube.com/embed/" + videoId;
    }
}