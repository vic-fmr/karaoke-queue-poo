 package com.karaoke.backend.services;

import com.karaoke.backend.dtos.YouTubeVideoDTO;
import com.karaoke.backend.models.Song;
import com.karaoke.backend.repositories.SongRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SongService {

    private final SongRepository songRepository;

    @Transactional
    public Song createSongFromVideo(YouTubeVideoDTO videoDTO) {
        Song song = new Song(
                videoDTO.videoId(),
                videoDTO.title(),
                "Artista Desconhecido",
                videoDTO.embedUrl()
        );
        Song savedSong = songRepository.save(song);
        System.out.println("LOG: Nova música criada no banco: " + savedSong.getTitle() +
                " (YouTube ID: " + savedSong.getYoutubeVideoId() + ")");
        return savedSong;
    }
}