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

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class KaraokeService {

    private final KaraokeSessionRepository sessionRepository;
    private final FilaService filaService;
    private final QueueItemRepository queueItemRepository;
    private final YoutubeService youTubeService;
    private final SongService songService;

    @Transactional
    public KaraokeSession createSession() {
        KaraokeSession newSession = new KaraokeSession();
        KaraokeSession savedSession = sessionRepository.save(newSession);
        System.out.println("LOG: Nova sessão criada! Codigo de Acesso: " + savedSession.getAccessCode());

        return savedSession;
    }

    // KaraokeService.java
    @Transactional
    public List<KaraokeSession> getAllSessions() {
        return sessionRepository.findAll();
    }

// Certifique-se de que o QueueItemDTO também não expõe dados sensíveis do User.

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
// Mudança na assinatura: agora recebe um YouTubeVideoDTO (o vídeo escolhido)
public void addSongToQueue(String accessCode, YouTubeVideoDTO selectedVideo, User user) {
    KaraokeSession session = getSession(accessCode);

    // --- 1. LÓGICA DE YOUTUBE (REMOVIDA A BUSCA) ---
    // A busca foi removida daqui. Confiamos que o Controller
    // nos passou o vídeo que o usuário selecionou.
    
    // Opcional: Se você quiser validar se o vídeo ainda existe ou pegar 
    // a duração exata, poderia chamar o youtubeService.getVideoDetails(id),
    // mas para performance, geralmente usamos o que o front mandou.

    // --- 2. LÓGICA DE USUÁRIO ---
    if (user.getSession() == null || !user.getSession().getId().equals(session.getId())) {
        session.addUser(user); 
    }

    // --- 3. LÓGICA DE MÚSICA ---
    // Reaproveita seu método existente, passando o vídeo recebido
    Song song = songService.createSongFromVideo(selectedVideo);

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