 package com.karaoke.backend.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.karaoke.backend.dtos.YouTubeVideoDTO;
import com.karaoke.backend.models.Song;
import com.karaoke.backend.repositories.SongRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SongService {

    private final SongRepository songRepository;

    @Transactional
    public Song createSongFromVideo(YouTubeVideoDTO videoDTO) {
    // Evita duplicatas: se já existe uma música com o mesmo youtubeVideoId, retorna-a
    return songRepository.findByYoutubeVideoId(videoDTO.getVideoId())
        .orElseGet(() -> {
            Song song = new Song(
                videoDTO.getVideoId(),
                videoDTO.getTitle(),
                "Artista Desconhecido",
                videoDTO.getEmbedUrl()
            );
            Song savedSong = songRepository.save(song);
            System.out.println("LOG: Nova música criada no banco: " + savedSong.getTitle() +
                " (YouTube ID: " + savedSong.getYoutubeVideoId() + ")");
            // Debug: imprimir stack trace para identificar chamadas duplicadas
            new Exception("Stack trace for song creation").printStackTrace(System.out);
            return savedSong;
        });
    }
}