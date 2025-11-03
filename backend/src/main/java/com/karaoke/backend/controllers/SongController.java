package com.karaoke.backend.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.karaoke.backend.dtos.YouTubeVideoDTO;
import com.karaoke.backend.services.YoutubeService;

@RestController
@RequestMapping("/api/videos")
public class SongController {

    @Autowired
    private YoutubeService youTubeService;

    @GetMapping("/search")
    public List<YouTubeVideoDTO> search(@RequestParam String query) {
        return youTubeService.searchVideos(query);
    }
}
