package com.karaoke.backend.controllers;

import java.net.URI;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.karaoke.backend.dtos.AddSongRequestDTO;
import com.karaoke.backend.models.KaraokeSession;
import com.karaoke.backend.services.KaraokeService;
import com.karaoke.backend.services.exception.SessionNotFoundException;
import com.karaoke.backend.services.exception.VideoNotFoundException;

@RestController
@RequestMapping("/api/sessions")
public class KaraokeController {

    @Autowired
    private KaraokeService service;
    @Autowired(required = false)
    private com.karaoke.backend.services.FilaService filaService;

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
        KaraokeSession session = service.getSession(sessionCode.toUpperCase());
        return ResponseEntity.ok(session);
    }

    // Endpoint temporário de debug: retorna a fila justa (fair queue) calculada pelo servidor.
    @GetMapping("/{sessionCode}/fairQueue")
    public ResponseEntity<com.karaoke.backend.dtos.FilaUpdateDTO> getFairQueue(@PathVariable String sessionCode) {
    KaraokeSession session = service.getSession(sessionCode.toUpperCase());
    java.util.List<com.karaoke.backend.models.QueueItem> fair = filaService == null
        ? java.util.List.of()
        : filaService.computeFairOrder(session);
        java.util.List<com.karaoke.backend.dtos.QueueItemDTO> dtoList = fair.stream()
                .map(com.karaoke.backend.dtos.QueueItemDTO::fromEntity)
                .toList();
        com.karaoke.backend.dtos.QueueItemDTO nowPlaying = dtoList.isEmpty() ? null : dtoList.get(0);
        com.karaoke.backend.dtos.FilaUpdateDTO dto = new com.karaoke.backend.dtos.FilaUpdateDTO(dtoList, nowPlaying,
                session.getStatus() == null ? null : session.getStatus().name());
        return ResponseEntity.ok(dto);
    }

@PostMapping("/{sessionCode}/queue")
    public ResponseEntity<Void> addSongToQueue(
        @PathVariable String sessionCode, 
        @RequestBody AddSongRequestDTO request // DTO que agora contém o TÍTULO
    ) {
        // O Service agora recebe o título da música
        service.addSongToQueue(
            sessionCode.toUpperCase(), 
            request.getSongTitle(),
            request.getUserId(), 
            request.getUserName()
        );
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{sessionCode}")
    public ResponseEntity<Void> endSession(@PathVariable String sessionCode){
        service.endSession(sessionCode.toUpperCase());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{sessionCode}/queue/{queueItemId}")
    public ResponseEntity<Void> deleteSongFromQueue(@PathVariable String sessionCode, @PathVariable String queueItemId){
        service.deleteSongFromQueue(sessionCode.toUpperCase(), queueItemId);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(SessionNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<String> handleSessionNotFound(SessionNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(VideoNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND) // Ou HttpStatus.UNPROCESSABLE_ENTITY (422)
    public ResponseEntity<String> handleVideoNotFound(VideoNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }
}
