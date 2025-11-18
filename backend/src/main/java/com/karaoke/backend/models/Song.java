package com.karaoke.backend.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
public class Song {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long songId;

    private String youtubeVideoId;
    private String title;
    private String artist;
    private String url;

    public Song(String youtubeVideoId, String title, String artist, String url) {
        this.youtubeVideoId = youtubeVideoId;
        this.title = title;
        this.artist = artist;
        this.url = url;
    }
}