package com.karaoke.backend.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
public class QueueItem {

    @Id
    private String queueItemId;

    @Column(nullable = false, updatable = false)
    private LocalDateTime timestampAdded;

    // Relacionamento com a sessão a que pertence
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id")
    @JsonIgnore
    private KaraokeSession session;

    // Relacionamento com o usuário que adicionou a música
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    // Relacionamento com a música que foi adicionada
    @ManyToOne
    @JoinColumn(name = "song_id")
    private Song song;

    public QueueItem(String queueItemId, User user, Song song) {
        this.queueItemId = queueItemId;
        this.user = user;
        this.song = song;
        this.timestampAdded = LocalDateTime.now();
    }
}