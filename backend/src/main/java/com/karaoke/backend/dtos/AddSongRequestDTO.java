package com.karaoke.backend.dtos;

import lombok.Data;

@Data
public class AddSongRequestDTO {
    private String youtubeUrl;
    private String userId;
    private String userName;
    private String songId;
}