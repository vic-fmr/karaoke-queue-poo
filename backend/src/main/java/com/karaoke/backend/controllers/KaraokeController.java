package com.karaoke.backend.controllers;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

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

import com.karaoke.backend.dtos.AddSongRequestDTO;
import com.karaoke.backend.dtos.QueueItemDTO;
import com.karaoke.backend.dtos.SessionResponseDTO;
import com.karaoke.backend.dtos.UserDTO;
import com.karaoke.backend.dtos.YouTubeVideoDTO;
import com.karaoke.backend.models.KaraokeSession;
import com.karaoke.backend.models.User;
import com.karaoke.backend.services.KaraokeService;
import com.karaoke.backend.services.FilaService; // Importe o FilaService

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
public class KaraokeController {

    private final KaraokeService service;
    private final FilaService filaService;
    private final com.karaoke.backend.repositories.UserRepository userRepository;

    @PostMapping
    public ResponseEntity<KaraokeSession> createSession(@AuthenticationPrincipal User host) {
        KaraokeSession newSession = service.createSession(host);
        URI location = URI.create(String.format("/api/sessions/%s", newSession.getAccessCode()));
        return ResponseEntity.created(location).body(newSession);
    }

    @GetMapping
    public ResponseEntity<List<KaraokeSession>> getAllSessions() {
        List<KaraokeSession> session = service.getAllSessions();
        return ResponseEntity.ok(session);
    }
    
    @GetMapping("/{sessionCode}")
    public ResponseEntity<SessionResponseDTO> getSession(@PathVariable String sessionCode) {
        SessionResponseDTO responseDTO = service.getSessionResponse(sessionCode);
        return ResponseEntity.ok(responseDTO);
    }

    // Endpoint temporário de debug: retorna a fila justa (fair queue) calculada pelo servidor.
    @GetMapping("/{sessionCode}/fairQueue")
    public ResponseEntity<com.karaoke.backend.dtos.FilaUpdateDTO> getFairQueue(@PathVariable String sessionCode) {
        KaraokeSession session = service.getSession(sessionCode.toUpperCase());
        
        // Calcula a fila justa
        java.util.List<com.karaoke.backend.models.QueueItem> fair = filaService.computeFairOrder(session);
        
        // Mapeia a fila para DTOs
        java.util.List<com.karaoke.backend.dtos.QueueItemDTO> dtoList = fair.stream()
                .map(com.karaoke.backend.dtos.QueueItemDTO::fromEntity)
                .toList();
        
        // Mapeia os usuários para DTOs
        List<UserDTO> userDTOs = session.getConnectedUsers().stream()
                .map(UserDTO::fromEntity)
                .collect(Collectors.toList());

        com.karaoke.backend.dtos.QueueItemDTO nowPlaying = dtoList.isEmpty() ? null : dtoList.get(0);
        
        // Constrói o DTO com os 4 argumentos corretos
        com.karaoke.backend.dtos.FilaUpdateDTO dto = new com.karaoke.backend.dtos.FilaUpdateDTO(
            dtoList, 
            nowPlaying,
            session.getStatus() == null ? null : session.getStatus().name(),
            userDTOs,
            session.getHost() != null ? session.getHost().getEmail() : null
        );
        
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/{sessionCode}/queue")
    public ResponseEntity<Void> addSongToQueue(
            @PathVariable String sessionCode,
            @RequestBody AddSongRequestDTO request,
            @AuthenticationPrincipal User user) {
        YouTubeVideoDTO videoEscolhido = new YouTubeVideoDTO();
        videoEscolhido.setVideoId(request.videoId());
        videoEscolhido.setTitle(request.title());
        videoEscolhido.setThumbnail(request.thumbnailUrl());

        service.addSongToQueue(
                sessionCode,
                videoEscolhido,
                user);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/{sessionCode}/queue/next")
    public ResponseEntity<Void> playNextSong(
            @PathVariable String sessionCode,
            @AuthenticationPrincipal User user) {
        
        KaraokeSession session = service.getSession(sessionCode);
        if (user == null || session.getHost() == null || !session.getHost().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        service.playNextSong(sessionCode);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/{sessionCode}")
    public ResponseEntity<Void> endSession(@PathVariable String sessionCode, @AuthenticationPrincipal User user) {
        KaraokeSession session = service.getSession(sessionCode);
        if (user == null || session.getHost() == null || !session.getHost().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        service.endSession(sessionCode);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{sessionCode}/queue/{queueItemId}")
    public ResponseEntity<Void> deleteSongFromQueue(
            @PathVariable String sessionCode, 
            @PathVariable Long queueItemId,
            @AuthenticationPrincipal User user) {
        
        KaraokeSession session = service.getSession(sessionCode);
        QueueItem queueItem = service.getQueueItem(queueItemId);
        Boolean isHost = session.getHost() != null && user != null && session.getHost().getId().equals(user.getId());
        Boolean isOwnerOfQueueItem = queueItem.getUser() != null && user != null && queueItem.getUser().getId().equals(user.getId());

        // Só pode deletar se for o host da sessão ou o próprio usuário que adicionou a música na fila
        if (
            !isHost && !isOwnerOfQueueItem
        ) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        service.deleteSongFromQueue(sessionCode, queueItemId);
        return ResponseEntity.noContent().build();
    }
}
