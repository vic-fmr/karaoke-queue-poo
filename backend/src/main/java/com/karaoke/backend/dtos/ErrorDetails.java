package com.karaoke.backend.dtos;

import java.time.LocalDateTime;

public record ErrorDetails(LocalDateTime timestamp, int status, String details, String message) {

    public ErrorDetails(int status, String error, String message) {
        this(LocalDateTime.now(), status, error, message);
    }
}
