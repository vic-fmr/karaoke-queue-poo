package com.karaoke.backend.repositories;

import com.karaoke.backend.models.Song;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SongRepository extends JpaRepository<Song, Long> { // O ID da Song é String (UUID)
}