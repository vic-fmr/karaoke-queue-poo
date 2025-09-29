package com.karaoke.backend.models;

import java.security.SecureRandom;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import lombok.Data;

@Data
public class KaraokeSession {

    // private final String sessionId;
    private final String accessCode;
    private final Map<String, User> connectedUsers;
    private final Queue<QueueItem> songQueue;

    private SessionStatus status; // Agora é ENUM

    public KaraokeSession() {
        // this.sessionId = UUID.randomUUID().toString();
        this.accessCode = generateAccessCode();

        this.connectedUsers = new ConcurrentHashMap<>();
        this.songQueue = new LinkedList<>();
        this.status = SessionStatus.WAITING; // Estado inicial
    }

    private String generateAccessCode() {
        final String ALFABETO = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        final int TAMANHO = 6;
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(TAMANHO);

        for (int i = 0; i < TAMANHO; i++) {
            int index = random.nextInt(ALFABETO.length());
            sb.append(ALFABETO.charAt(index));
        }
        return sb.toString();
    }

    public boolean addUser(User user) {
        if (connectedUsers.containsKey(user.getUserId())) {
            return false;
        }
        connectedUsers.put(user.getUserId(), user);
        return true;
    }

    //Terminar de ajustar o removeUser
    public boolean removeUser(String userId) {
        boolean userExisted = connectedUsers.remove(userId) != null;
        if (userExisted) {
            songQueue.removeIf(item -> item.getUser().getUserId().equals(userId));
        }
        return userExisted;
    }

    public void enqueueSong(QueueItem item) {
        songQueue.offer(item);
    }

    public QueueItem dequeueSong() {
        return songQueue.poll();
    }

    public QueueItem peekNextSong() {
        return songQueue.peek();
    }

    public List<QueueItem> getCurrentQueueList() {
        return new LinkedList<>(songQueue);
    }

    // ENUM interno (pode extrair para outro arquivo se preferir)
    public enum SessionStatus {
        WAITING,
        PLAYING,
        CLOSED
    }
}
