package com.karaoke.backend.services;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.karaoke.backend.dtos.YouTubeVideoDTO;
import com.karaoke.backend.exception.SessionNotFoundException;
import com.karaoke.backend.models.KaraokeSession;
import com.karaoke.backend.models.QueueItem;
import com.karaoke.backend.models.Song;
import com.karaoke.backend.models.User;
import com.karaoke.backend.repositories.KaraokeSessionRepository;
import com.karaoke.backend.repositories.QueueItemRepository;
import com.karaoke.backend.repositories.SongRepository;

import lombok.RequiredArgsConstructor;

import com.karaoke.backend.dtos.QueueItemDTO;
import com.karaoke.backend.dtos.SessionResponseDTO;
import com.karaoke.backend.dtos.UserDTO;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class KaraokeService {

    private final KaraokeSessionRepository sessionRepository;
    private final FilaService filaService;
    private final QueueItemRepository queueItemRepository;
    private final YoutubeService youTubeService;
    private final SongService songService;
    private final SongRepository songRepository;
    private final com.karaoke.backend.repositories.UserRepository userRepository;

    @Transactional(readOnly = true)
    public SessionResponseDTO getSessionResponse(String accessCode) {
        KaraokeSession session = getSession(accessCode);
        
        // Calcula a ordem justa dentro da transação
        List<QueueItem> fairOrder = filaService.computeFairOrder(session);
        
        List<QueueItemDTO> queueDTOs = fairOrder.stream()
                .map(QueueItemDTO::fromEntity)
                .collect(Collectors.toList());

        List<UserDTO> userDTOs = session.getConnectedUsers().stream()
                .map(UserDTO::fromEntity)
                .collect(Collectors.toList());

        QueueItemDTO nowPlaying = queueDTOs.isEmpty() ? null : queueDTOs.get(0);

        return new SessionResponseDTO(
                session.getId(),
                session.getAccessCode(),
                session.getStatus().name(),
                userDTOs,
                queueDTOs,
                nowPlaying,
                session.getHost() != null ? session.getHost().getEmail() : null
        );
    }

    @Transactional
    public KaraokeSession createSession(User host) {
        KaraokeSession newSession = new KaraokeSession();
        newSession.setHost(host);
        if (host != null) {
            newSession.addUser(host);
        }
        KaraokeSession savedSession = sessionRepository.save(newSession);
        System.out.println("LOG: Nova sessão criada! Codigo de Acesso: " + savedSession.getAccessCode() + 
                           (host != null ? " Host: " + host.getUsername() : ""));

        return savedSession;
    }

    @Transactional
    public KaraokeSession createSession() {
        return createSession(null);
    }

    @Transactional(readOnly = true)
    public List<KaraokeSession> getAllSessions() {
        return sessionRepository.findAll();
    }

    @Transactional(readOnly = true)
    public KaraokeSession getSession(String accessCode) {
        return sessionRepository.findByAccessCode(accessCode.toUpperCase())
                .orElseThrow(
                        () -> new SessionNotFoundException("Sessão com código '" + accessCode + "' não encontrada."));
    }

    @Transactional
    public void endSession(String accessCode) {
        KaraokeSession session = getSession(accessCode);
        sessionRepository.delete(session);
        System.out.println("LOG: Sessão finalizada: " + accessCode);
    }

    @Transactional
    public void addSongToQueue(String accessCode, YouTubeVideoDTO selectedVideo, User user) {
        KaraokeSession session = getSession(accessCode);

        if (user == null) {
            try {
                var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
                if (auth != null && auth.isAuthenticated() && !(auth instanceof org.springframework.security.authentication.AnonymousAuthenticationToken)) {
                    String username = auth.getName();
                    if (username != null) {
                        final String uname = username;
                        user = userRepository.findByUsername(uname).orElseGet(() -> {
                            com.karaoke.backend.models.User nu = new com.karaoke.backend.models.User();
                            nu.setUsername(uname);
                            com.karaoke.backend.models.User saved = userRepository.save(nu);
                            return saved;
                        });
                    }
                }
            } catch (Exception ignored) {
            }
        }

        if (user != null) {
            if (user.getSession() == null || !user.getSession().getId().equals(session.getId())) {
                session.addUser(user);
            }
        }

        Song song = songService.createSongFromVideo(selectedVideo);

        QueueItem queueItem = new QueueItem(session, user, song);
        session.addQueueItem(queueItem);

        String uid = user != null && user.getId() != null ? user.getId().toString() : "";
        List<String> rotation = session.getRotationUserIds();
        if (rotation == null) rotation = new java.util.ArrayList<>();

        if (rotation.isEmpty()) {
            if (rotation.isEmpty()) {
                rotation.add(uid);
                session.setNextUserIndex(0);
            } else if (!uid.isEmpty() && !rotation.contains(uid)) {
                // --- CORREÇÃO: Adiciona ao FINAL da rotação para não furar a fila de quem já espera ---
                rotation.add(uid);
            }
        session.setRotationUserIds(rotation);
        sessionRepository.save(session);

        filaService.notificarAtualizacaoFila(accessCode);
    }

    @Transactional
    public void deleteSongFromQueue(String accessCode, Long queueItemId) {
        KaraokeSession session = getSession(accessCode);
        Optional<QueueItem> itemOpt = queueItemRepository.findById(queueItemId);

        if (itemOpt.isPresent()) {
            QueueItem itemToDelete = itemOpt.get();

            List<com.karaoke.backend.models.QueueItem> fairOrder = filaService.computeFairOrder(session);
            Long nowPlayingId = fairOrder.isEmpty() ? null : fairOrder.get(0).getQueueItemId();
            if (nowPlayingId != null && nowPlayingId.equals(queueItemId)) {
                String userIdStr = itemToDelete.getUser() != null && itemToDelete.getUser().getId() != null
                        ? itemToDelete.getUser().getId().toString()
                        : "";
                List<String> rotation = session.getRotationUserIds();
                if (rotation != null && !rotation.isEmpty()) {
                    int idx = rotation.indexOf(userIdStr);
                    if (idx != -1) {
                        int nextIdx = (idx + 1) % rotation.size();
                        session.setNextUserIndex(nextIdx);
                    }
                }
                sessionRepository.save(session);
            }

            session.deleteQueueItem(itemToDelete);
        }

        filaService.notificarAtualizacaoFila(accessCode);
    }

    @Transactional
    public void addSongToQueue(String accessCode, YouTubeVideoDTO selectedVideo, java.security.Principal principal) {
        com.karaoke.backend.models.User user = null;
        if (principal != null) {
            String username = principal.getName();
            if (username != null) {
                final String uname = username;
                user = userRepository.findByUsername(uname).orElseGet(() -> {
                    com.karaoke.backend.models.User nu = new com.karaoke.backend.models.User();
                    nu.setUsername(uname);
                    return userRepository.save(nu);
                });
            }
        }
        addSongToQueue(accessCode, selectedVideo, user);
    }

    @Transactional
    public Optional<QueueItem> playNextSong(String accessCode) {
        KaraokeSession session = getSession(accessCode);
        List<QueueItem> fairOrder = filaService.computeFairOrder(session);
        if (fairOrder.isEmpty()) {
            return Optional.empty();
        }

        QueueItem nextItem = fairOrder.get(0);

        List<String> rotation = session.getRotationUserIds();
        if (rotation != null && !rotation.isEmpty()) {
            String currentUserId = nextItem.getUser() != null ? nextItem.getUser().getId().toString() : "";
            int currentIndexInRotation = rotation.indexOf(currentUserId);
            if (currentIndexInRotation != -1) {
                int nextIdx = (currentIndexInRotation + 1) % rotation.size();
                session.setNextUserIndex(nextIdx);
            }
        }

        session.deleteQueueItem(nextItem);
        sessionRepository.save(session);
        filaService.notificarAtualizacaoFila(accessCode);

        return Optional.of(nextItem);
    }

    @Transactional
    public KaraokeSession joinSession(String sessionCode, User user) {
        KaraokeSession session = getSession(sessionCode);
        if (!session.getConnectedUsers().contains(user)) {
            session.addUser(user);
            sessionRepository.save(session);
            filaService.notificarAtualizacaoFila(sessionCode);
        }
        return session;
    }

    @Transactional
    public void leaveSession(String sessionCode, User user) {
        KaraokeSession session = getSession(sessionCode);
        session.getConnectedUsers().remove(user);
        sessionRepository.save(session);
        filaService.notificarAtualizacaoFila(sessionCode);
    }
}