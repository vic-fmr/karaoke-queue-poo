package com.karaoke.backend.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
public class QueueItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long queueItemId;

    // Data/Hora gerada pelo Hibernate
    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime timestampAdded;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    @JsonIgnore
    private KaraokeSession session;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "song_id")
    private Song song;

    public QueueItem(KaraokeSession session, User user, Song song) {
        this.session = session;
        this.user = user;
        this.song = song;
    }
}