package com.karaoke.backend.repositories;

import com.karaoke.backend.models.QueueItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QueueItemRepository extends JpaRepository<QueueItem, String> {
}
