package com.karaoke.backend.dtos;

import java.util.List;

import com.karaoke.backend.models.User;

public record FilaUpdateDTO (List<QueueItemDTO> songQueue, QueueItemDTO nowPlaying, String sessionStatus, List<UserDTO> connectedUsers) {

}