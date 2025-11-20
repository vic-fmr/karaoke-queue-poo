package com.karaoke.backend.dtos;

import com.karaoke.backend.models.QueueItem; // Importante se não estiver no mesmo pacote

public record QueueItemDTO(String songTitle, String youtubeLink, String addedByUserName, Long queueItemId) {

    public static QueueItemDTO fromEntity(QueueItem item) {
        // Verificações de nulidade para evitar NullPointerException (Opcional mas recomendado)
        String title = item.getSong() != null ? item.getSong().getTitle() : "Desconhecido";
        String videoId = item.getSong() != null ? item.getSong().getYoutubeVideoId() : ""; // <--- USE O ID AQUI
        String username = item.getUser() != null ? item.getUser().getUsername() : "Anônimo";
        Long id = item.getQueueItemId();

        return new QueueItemDTO(
            title,
            videoId, // Passa o ID (ex: dQw4w9WgXcQ) para o campo youtubeLink
            username,
            id
        );
    }
}