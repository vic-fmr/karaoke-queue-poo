package com.karaoke.backend.controllers;

import com.karaoke.backend.dtos.YouTubeVideoDTO;
import com.karaoke.backend.services.YouTubeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/resolve-video")
public class YouTubeController {

    @Autowired
    private YouTubeService youTubeService;


    @GetMapping
    public ResponseEntity<YouTubeVideoDTO> resolveBestVideo(@RequestParam String query) {
        
        List<YouTubeVideoDTO> validResults = youTubeService.searchVideos(query);

        if (!validResults.isEmpty()) {
            YouTubeVideoDTO bestVideo = validResults.get(0);
            
            
            return ResponseEntity.ok(bestVideo);
        } else {
            return ResponseEntity.notFound().build(); 
        }
    }
}
