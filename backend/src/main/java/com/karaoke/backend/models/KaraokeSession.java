package com.karaoke.backend.models;

import jakarta.persistence.*;
import lombok.Data;
// import lombok.NoArgsConstructor;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
// @NoArgsConstructor
public class KaraokeSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 6)
    private String accessCode;

    @Enumerated(EnumType.STRING)
    private SessionStatus status;

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<User> connectedUsers = new ArrayList<>();

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("timestampAdded ASC")
    private List<QueueItem> songQueue = new ArrayList<>();


    public KaraokeSession() {
        this.accessCode = generateAccessCode();
        this.status = SessionStatus.WAITING;
    }

    // O metodo generateAccessCode() continua o mesmo...
    private String generateAccessCode() {
        final String ALFABETO = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        final int TAMANHO = 6;
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(TAMANHO);
        for (int i = 0; i < TAMANHO; i++) {
            sb.append(ALFABETO.charAt(random.nextInt(ALFABETO.length())));
        }
        return sb.toString();
    }

    // Métodos de ajuda para gerenciar as listas de forma segura
    public void addUser(User user) {
        connectedUsers.add(user);
        user.setSession(this);
    }

    public void removeUser(User user) {
        connectedUsers.remove(user);
        user.setSession(null);
    }

    public void addQueueItem(QueueItem item) {
        songQueue.add(item);
        item.setSession(this);
    }

    public void deleteQueueItem(QueueItem item) {
        songQueue.remove(item);
        item.setSession(null);
    }

    public enum SessionStatus {

        WAITING,

        PLAYING,

        CLOSED

    }
}