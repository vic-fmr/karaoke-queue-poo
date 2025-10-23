    package com.karaoke.backend.repositories;

    import com.karaoke.backend.models.Song;
    import org.junit.jupiter.api.Test;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
    import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

    import java.util.Optional;
    import java.util.UUID;

    import static org.assertj.core.api.Assertions.assertThat;

    @DataJpaTest
    class SongRepositoryTest {

        @Autowired
        private TestEntityManager entityManager;

        @Autowired
        private SongRepository repository;

        @Test
        void save_ShouldPersistSong() {
            String songId = UUID.randomUUID().toString();
            Song newSong = new Song(songId, "Bohemian Rhapsody", "Queen");

            Song savedSong = repository.save(newSong);

            assertThat(savedSong.getSongId()).isEqualTo(songId);
            assertThat(savedSong.getTitle()).isEqualTo("Bohemian Rhapsody");
            assertThat(savedSong.getArtist()).isEqualTo("Queen");

            Optional<Song> foundById = repository.findById(songId);
            assertThat(foundById).isPresent();
            assertThat(foundById.get().getTitle()).isEqualTo("Bohemian Rhapsody");
        }

        @Test
        void findById_ShouldReturnSong_WhenExists() {
            String songId = UUID.randomUUID().toString();
            Song song = new Song(songId, "Stairway to Heaven", "Led Zeppelin");
            entityManager.persistAndFlush(song);

            Optional<Song> foundSong = repository.findById(songId);

            assertThat(foundSong).isPresent();
            assertThat(foundSong.get().getSongId()).isEqualTo(songId);
            assertThat(foundSong.get().getArtist()).isEqualTo("Led Zeppelin");
        }

        @Test
        void delete_ShouldRemoveSong() {
            String songId = UUID.randomUUID().toString();
            Song song = new Song(songId, "Like a Rolling Stone", "Bob Dylan");
            entityManager.persistAndFlush(song);

            repository.deleteById(songId);
            entityManager.flush();

            Optional<Song> foundSong = repository.findById(songId);
            assertThat(foundSong).isNotPresent();
        }
    }
