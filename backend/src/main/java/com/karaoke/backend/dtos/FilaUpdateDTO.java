package com.karaoke.backend.dtos;

import java.util.List;

public record FilaUpdateDTO (List<QueueItemDTO> songQueue, QueueItemDTO nowPlaying, String sessionStatus) {

}