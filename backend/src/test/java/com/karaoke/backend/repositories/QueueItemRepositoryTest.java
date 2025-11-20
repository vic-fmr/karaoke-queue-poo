package com.karaoke.backend.repositories;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import com.karaoke.backend.models.KaraokeSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import com.karaoke.backend.models.QueueItem;
import com.karaoke.backend.models.Song;
import com.karaoke.backend.models.User;

@DataJpaTest
class QueueItemRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private QueueItemRepository repository;

    private KaraokeSession testSession;

    @BeforeEach
    public void setup() {
        testSession = new KaraokeSession();
        entityManager.persist(testSession);
        entityManager.flush();
    }

    // --- UTILS PARA GARANTIR OS OBJETOS CORRETOS ---

    private User createTestUser(String username) {
        User user = new User();
        user.setUsername(username);
        return entityManager.persist(user);
    }

    // O construtor Song (String youtubeVideoId, String title, String artist, String url)
    private Song createTestSong(String title) {
        Song song = new Song("YT_ID_" + title, title, "Artist Name", "http://url.test");
        return entityManager.persist(song);
    }
    // --- FIM UTILS ---


    @Test
    void save_ShouldPersistQueueItem() {
        // 1. Arrange (Preparação)
        User user = createTestUser("Save User");
        Song song = createTestSong("Test Song 1");

        // 2. Act (Ação)
        // Construtor: new QueueItem(KaraokeSession session, User user, Song song)
        QueueItem newItem = new QueueItem(testSession, user, song);
        QueueItem savedItem = repository.save(newItem); // ID gerado aqui

        // 3. Assert (Verificação)
        assertThat(savedItem).isNotNull();
        // O ID é gerado pelo DB, não podemos compará-lo com o ID da sessão
        assertThat(savedItem.getQueueItemId()).isNotNull();
        assertThat(savedItem.getUser().getUsername()).isEqualTo("Save User");
        assertThat(savedItem.getSong().getTitle()).isEqualTo("Test Song 1");
        assertThat(savedItem.getTimestampAdded()).isNotNull();

        // 4. Verificação final pelo ID gerado
        Optional<QueueItem> foundById = repository.findById(savedItem.getQueueItemId());
        assertThat(foundById).isPresent();
        assertThat(foundById.get().getQueueItemId()).isEqualTo(savedItem.getQueueItemId());
    }

    @Test
    void findById_ShouldReturnQueueItem_WhenExists() {
        // 1. Arrange (Preparação)
        User user = createTestUser("Find User");
        Song song = createTestSong("Test Song 2");

        QueueItem item = new QueueItem(testSession, user, song);
        // PersistAndFlush para garantir que o ID (Long) seja gerado
        entityManager.persistAndFlush(item);

        Long generatedId = item.getQueueItemId();

        // 2. Act (Ação)
        Optional<QueueItem> foundItem = repository.findById(generatedId);

        // 3. Assert (Verificação)
        assertThat(foundItem).isPresent();
        assertThat(foundItem.get().getQueueItemId()).isEqualTo(generatedId);
        assertThat(foundItem.get().getUser().getUsername()).isEqualTo("Find User");
    }

    @Test
    void delete_ShouldRemoveQueueItem() {
        // 1. Arrange (Preparação)
        User user = createTestUser("Delete User");
        Song song = createTestSong("Test Song 3");

        QueueItem item = new QueueItem(testSession, user, song);
        entityManager.persistAndFlush(item);

        Long itemIdToDelete = item.getQueueItemId();

        // 2. Act (Ação)
        repository.deleteById(itemIdToDelete);
        entityManager.flush(); // Garante que a operação DELETE foi executada no banco

        // 3. Assert (Verificação)
        Optional<QueueItem> foundItem = repository.findById(itemIdToDelete);
        assertThat(foundItem).isNotPresent();
    }
}