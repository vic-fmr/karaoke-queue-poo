package com.karaoke.backend.repositories;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
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

    @Test
    void save_ShouldPersistQueueItem() {
        User user = new User();
        user.setUsername("Test User");
        entityManager.persist(user); 

        // CORRIGIDO: Adicionando o youtubeVideoId (o segundo argumento)
        Song song = new Song(
            UUID.randomUUID().toString(), 
            "TESTE_YOUTUBE_ID_1", 
            "Test Song", 
            "Test Artist"
        );
        entityManager.persist(song); 
        entityManager.flush(); 

        String queueItemId = UUID.randomUUID().toString();
        QueueItem newItem = new QueueItem(queueItemId, user, song);

        QueueItem savedItem = repository.save(newItem);

        assertThat(savedItem).isNotNull();
        assertThat(savedItem.getQueueItemId()).isEqualTo(queueItemId);
        assertThat(savedItem.getUser()).isEqualTo(user);
        assertThat(savedItem.getSong()).isEqualTo(song);
        assertThat(savedItem.getTimestampAdded()).isNotNull();

        Optional<QueueItem> foundById = repository.findById(queueItemId);
        assertThat(foundById).isPresent();
        assertThat(foundById.get().getQueueItemId()).isEqualTo(queueItemId); 
        assertThat(foundById.get().getUser().getUsername()).isEqualTo("Test User");
        assertThat(foundById.get().getSong().getTitle()).isEqualTo("Test Song");
    }

     @Test
    void findById_ShouldReturnQueueItem_WhenExists() {
        // CORRIGIDO: User deve ter os argumentos do construtor completo (assumindo songId, username)
        // Se user não tiver um construtor com argumentos, use setters.
        // Assumindo um construtor (String username):
        User user = new User();
        user.setUsername("Another User"); 
        entityManager.persist(user);
        
        // CORRIGIDO: Adicionando o youtubeVideoId (o segundo argumento)
        Song song = new Song(
            UUID.randomUUID().toString(), 
            "TESTE_YOUTUBE_ID_2",
            "Another Song", 
            "Another Artist"
        );
        entityManager.persist(song);
        entityManager.flush();

        String queueItemId = UUID.randomUUID().toString();
        QueueItem item = new QueueItem(queueItemId, user, song);
        entityManager.persistAndFlush(item);

        Optional<QueueItem> foundItem = repository.findById(queueItemId);

        assertThat(foundItem).isPresent();
        assertThat(foundItem.get().getQueueItemId()).isEqualTo(queueItemId); 
    }

    @Test
    void delete_ShouldRemoveQueueItem() {
        // CORRIGIDO: User deve ter os argumentos do construtor completo (assumindo songId, username)
        User user = new User();
        user.setUsername("Delete User");
        entityManager.persist(user);
        
        // CORRIGIDO: Adicionando o youtubeVideoId (o segundo argumento)
        Song song = new Song(
            UUID.randomUUID().toString(), 
            "TESTE_YOUTUBE_ID_3",
            "Delete Song", 
            "Delete Artist"
        );
        entityManager.persist(song);
        entityManager.flush();

        String queueItemId = UUID.randomUUID().toString();
        QueueItem item = new QueueItem(queueItemId, user, song);
        entityManager.persistAndFlush(item);

        repository.deleteById(queueItemId);
        entityManager.flush();

        Optional<QueueItem> foundItem = repository.findById(queueItemId);
        assertThat(foundItem).isNotPresent();
    }
}