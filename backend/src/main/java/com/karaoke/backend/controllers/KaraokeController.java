package com.karaoke.backend.controllers;

import com.karaoke.backend.dtos.AddSongRequest;
import com.karaoke.backend.models.KaraokeSession;
import com.karaoke.backend.services.KaraokeService;
import com.karaoke.backend.services.exception.SessionNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sessions")
public class KaraokeController {

    @Autowired
    private KaraokeService service;

    @PostMapping
    public ResponseEntity<KaraokeSession> createSession(){
        KaraokeSession newSession = service.createSession();
        return ResponseEntity.ok(newSession);
    }


    @GetMapping("/{sessionCode}")
    public ResponseEntity<KaraokeSession> getSession(@PathVariable String sessionCode){
        KaraokeSession session = service.getSession(sessionCode.toUpperCase());
        return ResponseEntity.ok(session);
    }

    @PostMapping("/{sessionCode}/queue")
    public ResponseEntity<Void> addSongToQueue(@PathVariable String sessionCode, @RequestBody AddSongRequest request){
        service.addSongToQueue(sessionCode.toUpperCase(), request.getYoutubeUrl(), request.getUserId());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{sessionCode}")
    public ResponseEntity<Void> endSession(@PathVariable String sessionCode){
        service.endSession(sessionCode.toUpperCase());
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(SessionNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<String> handleSessionNotFound(SessionNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
}
}
