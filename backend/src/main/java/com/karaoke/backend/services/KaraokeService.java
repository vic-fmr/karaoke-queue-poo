package com.karaoke.backend.services;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
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

@Service
public class KaraokeService {

    @Autowired
    private KaraokeSessionRepository sessionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SongRepository songRepository;

    @Autowired
    private FilaService filaService;

    @Autowired
    private QueueItemRepository queueItemRepository;

    @Autowired
    private YouTubeService youTubeService;

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

        // --- LÓGICA DE INTEGRAÇÃO COM YOUTUBE (PASSO NOVO) ---

        String searchQuery = songTitle + " karaoke";
        List<YouTubeVideoDTO> validVideos = youTubeService.searchVideos(searchQuery);

        if (validVideos.isEmpty()) {
            if (validVideos.isEmpty()) {
                throw new VideoNotFoundException(
                        "Não foi encontrado um vídeo válido e incorporável do YouTube para a música: " + songTitle);
            }
        }

        // Pega o primeiro resultado (o melhor e já validado)
        YouTubeVideoDTO bestVideo = validVideos.get(0);

        // --- LÓGICA DE USUÁRIO E PERSISTÊNCIA (EXISTENTE) ---

        // 2. Converte o userId (String) para Long ANTES de buscar
        Long userIdAsLong;
        try {
            userIdAsLong = Long.parseLong(userId);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("ID do usuário inválido: " + userId);
        }

        // 3. Busca/Cria o usuário
        User user = userRepository.findById(userIdAsLong).orElseGet(() -> {
            User newUser = new User();
            newUser.setUsername(userName);
            newUser.setSession(session);
            return userRepository.save(newUser);
        });

        // 4. Cria a Entidade Song com os dados OBTIDOS DO YOUTUBE
        Song song = new Song(
                UUID.randomUUID().toString(), // 1. songId (novo ID interno)
                bestVideo.videoId(), // 2. youtubeVideoId (ID do YouTube)
                bestVideo.title(), // 3. title (Título do YouTube)
                "Artista Desconhecido" // 4. artist (Placeholder, pois a busca não retorna)
        );

        // **!!! IMPORTANTE: Você precisa que o campo youtubeVideoId exista na sua
        // Entidade Song !!!**
        song.setYoutubeVideoId(bestVideo.videoId()); // Adiciona o ID do vídeo para persistência

        songRepository.save(song);

        // 5. Adiciona à Fila
        QueueItem queueItem = new QueueItem(UUID.randomUUID().toString(), user, song);
        session.addQueueItem(queueItem);
        sessionRepository.save(session);

        System.out.println("LOG: Música adicionada à fila da sessão " + accessCode);

        // 6. Notifica o Front-end
        filaService.notificarAtualizacaoFila(accessCode);
    }

    @Transactional
    public void deleteSongFromQueue(String accessCode, String queueItemId) {
        // 1. Buscamos o item da fila para garantir que ele exista.
        Optional<QueueItem> itemOpt = queueItemRepository.findById(queueItemId);
        KaraokeSession session = getSession(accessCode);

        if (itemOpt.isPresent()) {
            QueueItem itemToDelete = itemOpt.get();
            // A sessão não precisa ser buscada explicitamente se a QueueItem não for
            // bidirecionalmente mapeada com a sessão.
            // Deletamos o QueueItem diretamente.

            session.deleteQueueItem(itemToDelete);

            // 2. Deleta o QueueItem pela entidade
            queueItemRepository.delete(itemToDelete);

            System.out.println("LOG: Item de fila (" + queueItemId + ") removido da sessão " + accessCode);
        } else {
            System.out.println("ALERTA: Tentativa de remover item de fila não existente com ID: " + queueItemId);
        }

        // 3. Notifica todos os clientes via WebSocket
        filaService.notificarAtualizacaoFila(accessCode);
    }
}