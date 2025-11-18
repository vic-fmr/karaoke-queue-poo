package com.karaoke.backend.dtos;

import com.karaoke.backend.models.QueueItem;

import java.util.List;

public record SessionResponseDTO(Long id, String accessCode, String status, List<UserDTO> connectedUsers, List<QueueItemDTO> songQueue) {
}
