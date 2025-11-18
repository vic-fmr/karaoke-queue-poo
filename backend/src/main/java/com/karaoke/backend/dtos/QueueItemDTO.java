package com.karaoke.backend.dtos;


public record QueueItemDTO(String songTitle, String youtubeLink, String addedByUserName, Long queueItemId) {

    public static QueueItemDTO fromEntity(com.karaoke.backend.models.QueueItem item) {

        return new QueueItemDTO(
            item.getSong().getTitle(),
            item.getSong().getUrl(),
            item.getUser().getUsername(),
            item.getQueueItemId()
        );
    }
}