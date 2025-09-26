/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.karaoke.backend.model;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import lombok.Data;

@Data
public class KaraokeSession {

    private final String sessionId;
    private final String accessCode;
    private final Map<String, User> connectedUsers;

    private final Queue<QueueItem> songQueue;

    // Transformar em ENUM
    private String status; // Ex: "WAITING", "PLAYING", "CLOSED"

    public KaraokeSession(String accessCode) {
        // Gera um ID único e seguro para a sessão
        this.sessionId = UUID.randomUUID().toString();
        this.accessCode = accessCode;

        // Inicializa as estruturas de dados essenciais
        this.connectedUsers = new ConcurrentHashMap<>();
        this.songQueue = new LinkedList<>();
        this.status = "WAITING"; // Estado inicial
    }

    public boolean addUser(User user) {
        if (connectedUsers.containsKey(user.getUserId())) {
            return false;
        }
        connectedUsers.put(user.getUserId(), user);
        return true;
    }

    public boolean removeUser(String userId) {
        // Implementar lógica para remover os QueueItem dele também.
        return connectedUsers.remove(userId) != null;
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

}
