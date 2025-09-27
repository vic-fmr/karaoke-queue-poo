package com.karaoke.backend.dtos;

import lombok.Data;


@Data
public class AddSongRequest {
    private String youtubeUrl;
    private String userId;
}
