package com.karaoke.backend.repositories;

import java.util.Optional;
// UUID não é mais necessário
// import java.util.UUID; 

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import com.karaoke.backend.models.Song;

@DataJpaTest
class SongRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private SongRepository repository;

    @Test
    void save_ShouldPersistSong() {
        // 1. ARRANGE: Criamos a música SEM ID (passamos null ou usamos o construtor sem ID)
        // Usando o construtor: Song(youtubeVideoId, title, artist, url)
        Song newSong = new Song(
            "FAKE_ID_1", 
            "Bohemian Rhapsody", 
            "Queen",
            "http://url-exemplo.com"
        );

        // 2. ACT: Salvamos
        Song savedSong = repository.save(newSong);

        // 3. ASSERT: Verificamos se o ID foi gerado (não é nulo e é maior que 0)
        assertThat(savedSong.getSongId()).isNotNull(); 
        assertThat(savedSong.getSongId()).isGreaterThan(0L); // Garante que é um Long válido
        
        assertThat(savedSong.getTitle()).isEqualTo("Bohemian Rhapsody");
        assertThat(savedSong.getArtist()).isEqualTo("Queen");
        assertThat(savedSong.getYoutubeVideoId()).isEqualTo("FAKE_ID_1");
    }

    @Test
    void findById_ShouldReturnSong_WhenExists() {
        // 1. ARRANGE
        Song song = new Song(
            "FAKE_ID_2", 
            "Stairway to Heaven", 
            "Led Zeppelin",
            "http://url-exemplo.com"
        );
        
        // Persistimos no banco para gerar o ID
        Song persistedSong = entityManager.persistAndFlush(song);
        Long generatedId = persistedSong.getSongId(); // Pegamos o ID que o banco criou

        // 2. ACT
        // Buscamos usando o ID gerado (Long)
        Optional<Song> foundSong = repository.findById(generatedId);

        // 3. ASSERT
        assertThat(foundSong).isPresent();
        assertThat(foundSong.get().getSongId()).isEqualTo(generatedId);
        assertThat(foundSong.get().getArtist()).isEqualTo("Led Zeppelin");
    }

    @Test
    void delete_ShouldRemoveSong() {
        // 1. ARRANGE
        Song song = new Song(
            "FAKE_ID_3", 
            "Like a Rolling Stone", 
            "Bob Dylan",
            "http://url-exemplo.com"
        );
        
        Song persistedSong = entityManager.persistAndFlush(song);
        Long generatedId = persistedSong.getSongId();

        // 2. ACT
        repository.deleteById(generatedId); // Deletamos pelo ID Long
        entityManager.flush(); // Garante a execução do delete

        // 3. ASSERT
        Optional<Song> foundSong = repository.findById(generatedId);
        assertThat(foundSong).isNotPresent();
    }
}