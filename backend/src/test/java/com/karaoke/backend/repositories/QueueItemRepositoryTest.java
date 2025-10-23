package com.karaoke.backend.repositories;

import com.karaoke.backend.models.QueueItem;
import com.karaoke.backend.models.Song;
import com.karaoke.backend.models.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

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

        Song song = new Song(UUID.randomUUID().toString(), "Test Song", "Test Artist");
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
        User user = new User("user2", "Another User");
        entityManager.persist(user);
        Song song = new Song(UUID.randomUUID().toString(), "Another Song", "Another Artist");
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
        User user = new User("user3", "Delete User");
        entityManager.persist(user);
        Song song = new Song(UUID.randomUUID().toString(), "Delete Song", "Delete Artist");
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