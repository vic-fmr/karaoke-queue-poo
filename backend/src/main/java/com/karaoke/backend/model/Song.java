/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.karaoke.backend.model;

import java.util.UUID;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor // Para o Spring Boot
public class Song {
    
    private final String songId; 
    
    private String title;
    private String artist;

    // ATRIBUTOS FUTUROS (da API do YouTube)
    // private String videoId; 
    // private int durationSeconds; 
    // private String thumbnailUrl;
    

    public Song(String title, String artist) {
        this.songId = UUID.randomUUID().toString();
        
        this.title = title;
        this.artist = artist;
    }
}