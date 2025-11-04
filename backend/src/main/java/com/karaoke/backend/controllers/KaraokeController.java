package com.karaoke.backend.controllers;

import java.net.URI;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.karaoke.backend.models.User;
import com.karaoke.backend.dtos.AddSongRequestDTO;
import com.karaoke.backend.models.KaraokeSession;
import com.karaoke.backend.services.KaraokeService;


@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
public class KaraokeController {

    private final KaraokeService service;


    @PostMapping
    public ResponseEntity<KaraokeSession> createSession() {
        KaraokeSession newSession = service.createSession();
        URI location = URI.create(String.format("/api/sessions/%s", newSession.getAccessCode()));
        return ResponseEntity.created(location).body(newSession);
    }


    @GetMapping
    public ResponseEntity<List<KaraokeSession>> getAllSessions(){
        List<KaraokeSession> session = service.getAllSessions();
        return ResponseEntity.ok(session);
    }

    @GetMapping("/{sessionCode}")
    public ResponseEntity<KaraokeSession> getSession(@PathVariable String sessionCode){
        KaraokeSession session = service.getSession(sessionCode);
        return ResponseEntity.ok(session);
    }

    @PostMapping("/{sessionCode}/queue")
    public ResponseEntity<Void> addSongToQueue(
        @PathVariable String sessionCode, 
        @RequestBody AddSongRequestDTO request,
        @AuthenticationPrincipal User authenticatedUser
    ) {
        service.addSongToQueue(
            sessionCode,
            request.songTitle(),
            authenticatedUser
        );
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/{sessionCode}")
    public ResponseEntity<Void> endSession(@PathVariable String sessionCode){
        service.endSession(sessionCode);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{sessionCode}/queue/{queueItemId}")
    public ResponseEntity<Void> deleteSongFromQueue(@PathVariable String sessionCode, @PathVariable Long queueItemId){
        service.deleteSongFromQueue(sessionCode, queueItemId);
        return ResponseEntity.noContent().build();
    }
}
