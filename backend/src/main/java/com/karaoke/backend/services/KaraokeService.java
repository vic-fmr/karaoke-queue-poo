package com.karaoke.backend.services;

import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.karaoke.backend.dtos.YouTubeVideoDTO;
import com.karaoke.backend.models.KaraokeSession;
import com.karaoke.backend.models.QueueItem;
import com.karaoke.backend.models.Song;
import com.karaoke.backend.models.User;
import com.karaoke.backend.repositories.KaraokeSessionRepository;
import com.karaoke.backend.repositories.QueueItemRepository;
import com.karaoke.backend.repositories.SongRepository;
import com.karaoke.backend.repositories.UserRepository;
import com.karaoke.backend.services.exception.SessionNotFoundException;
import com.karaoke.backend.services.exception.VideoNotFoundException;

@RequiredArgsConstructor
@Service
public class KaraokeService {

    private final KaraokeSessionRepository sessionRepository;
    private final SongRepository songRepository;
    private final FilaService filaService;
    private final QueueItemRepository queueItemRepository;
    private final YoutubeService youTubeService;
    private final UserService userService;

    @Transactional
    public KaraokeSession createSession() {
        KaraokeSession newSession = new KaraokeSession();
        KaraokeSession savedSession = sessionRepository.save(newSession);
        System.out.println("LOG: Nova sessão criada! Codigo de Acesso: " + savedSession.getAccessCode());

        return savedSession;
    }

    @Transactional
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
    public void addSongToQueue(String accessCode, String songTitle, String userId, String userName) {
        KaraokeSession session = getSession(accessCode);

        // --- 1. LÓGICA DE INTEGRAÇÃO COM YOUTUBE ---
        String searchQuery = songTitle + " karaoke";
        List<YouTubeVideoDTO> validVideos = youTubeService.searchVideos(searchQuery);

        if (validVideos.isEmpty()) {
            throw new VideoNotFoundException("Não foi encontrado um vídeo válido e incorporável...");
        }

        YouTubeVideoDTO bestVideo = validVideos.getFirst();

        // --- 2. LÓGICA DE USUÁRIO ---
        User user = userService.getOrCreateUser(userId, userName, session);

        // --- 3. LÓGICA DE MÚSICA ---
        Song song = new Song(
                bestVideo.videoId(),
                bestVideo.title(),
                "Artista Desconhecido"
        );

        songRepository.save(song);
        System.out.println("LOG: Nova música criada no banco: " + song.getTitle() + " (YouTube ID: " + song.getYoutubeVideoId() + ")");

        // --- 4. ADICIONA À FILA ---
        QueueItem queueItem = new QueueItem(session, user, song);
        session.addQueueItem(queueItem);
        sessionRepository.save(session);
        System.out.println("LOG: Música adicionada à fila da sessão " + accessCode);

        // --- 5. NOTIFICAÇÃO ---
        filaService.notificarAtualizacaoFila(accessCode);
    }

    @Transactional
    public void deleteSongFromQueue(String accessCode, Long queueItemId) {
        Optional<QueueItem> itemOpt = queueItemRepository.findById(queueItemId);
        KaraokeSession session = getSession(accessCode);

        if (itemOpt.isPresent()) {
            QueueItem itemToDelete = itemOpt.get();
            session.deleteQueueItem(itemToDelete);
            System.out.println("LOG: Item de fila (" + queueItemId + ") removido da sessão " + accessCode);
        } else {
            System.out.println("ALERTA: Tentativa de remover item de fila não existente com ID: " + queueItemId);
        }

        // Notifica todos os clientes via WebSocket
        filaService.notificarAtualizacaoFila(accessCode);
    }
}