package com.karaoke.backend.services.exception;

public class VideoNotFoundException extends RuntimeException {

    public VideoNotFoundException(String message) {
        super(message);
    }
}
