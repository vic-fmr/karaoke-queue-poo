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
    // Se o controlador não nos forneceu o User (por exemplo em testes com @WithMockUser),
    // tentamos recuperar o nome do usuário a partir do SecurityContext e criar/recuperar
    // o usuário na base de dados.
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
                        System.out.println("LOG: Created user from principal: " + uname + " (id=" + saved.getId() + ")");
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

    // --- 3. LÓGICA DE MÚSICA ---
    // Reaproveita seu método existente, passando o vídeo recebido
    System.out.println("LOG: songCountBefore=" + songRepository.count());
    Song song = songService.createSongFromVideo(selectedVideo);

    // --- 4. ADICIONA À FILA ---
    QueueItem queueItem = new QueueItem(session, user, song);
    session.addQueueItem(queueItem);

    // Atualiza ordem de rotação: se o usuário ainda não estiver presente, insere-o
    // imediatamente após o usuário apontado por nextUserIndex, para que o novato
    // entre na rotação logo após a posição atual.
    String uid = user.getId() != null ? user.getId().toString() : "";
    List<String> rotation = session.getRotationUserIds();
    if (rotation == null) rotation = new java.util.ArrayList<>();

    if (rotation.isEmpty()) {
        rotation.add(uid);
        session.setNextUserIndex(0);
    } else if (!rotation.contains(uid)) {
        int insertPos = session.getNextUserIndex() + 1;
        if (insertPos < 0) insertPos = 0;
        if (insertPos > rotation.size()) insertPos = rotation.size();
        rotation.add(insertPos, uid);
    }

    session.setRotationUserIds(rotation);

    sessionRepository.save(session);

    System.out.println("LOG: songCountAfter=" + songRepository.count());

    System.out.println("LOG: Música adicionada à fila da sessão " + accessCode);

    // --- 5. NOTIFICAÇÃO ---
    filaService.notificarAtualizacaoFila(accessCode);
    System.out.println("LOG: songCountAfterNotification=" + songRepository.count());
    }

    @Transactional
    public void deleteSongFromQueue(String accessCode, Long queueItemId) {
        KaraokeSession session = getSession(accessCode);
        Optional<QueueItem> itemOpt = queueItemRepository.findById(queueItemId);

        if (itemOpt.isPresent()) {
            QueueItem itemToDelete = itemOpt.get();

            // Se o item a ser deletado for o que está atualmente como nowPlaying na fila justa,
            // avançamos o ponteiro de rotação para o próximo usuário.
            List<com.karaoke.backend.models.QueueItem> fairOrder = filaService.computeFairOrder(session);
            Long nowPlayingId = fairOrder.isEmpty() ? null : fairOrder.get(0).getQueueItemId();
            if (nowPlayingId != null && nowPlayingId.equals(queueItemId)) {
                // Avança nextUserIndex para o usuário seguinte na rotação
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
            System.out.println("LOG: Item de fila (" + queueItemId + ") removido da sessão " + accessCode);
        } else {
            System.out.println("ALERTA: Tentativa de remover item de fila não existente com ID: " + queueItemId);
        }

        // Notifica todos os clientes via WebSocket
        filaService.notificarAtualizacaoFila(accessCode);
    }
    // Overload que aceita Principal (usado pelos controllers para encaminhar a identificação do usuário
    // sem ler diretamente o SecurityContext, o que evita efeitos colaterais entre testes).
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
                    com.karaoke.backend.models.User saved = userRepository.save(nu);
                    System.out.println("LOG: Created user from principal: " + uname + " (id=" + saved.getId() + ")");
                    return saved;
                });
            }
        }

        // Delega para a implementação existente
        addSongToQueue(accessCode, selectedVideo, user);
    }

    @Transactional
    public Optional<QueueItem> playNextSong(String accessCode) {
        KaraokeSession session = getSession(accessCode);

        // Compute fair order and remove the item that is playing (first of fair order)
        List<QueueItem> fairOrder = filaService.computeFairOrder(session);
        if (fairOrder.isEmpty()) {
            System.out.println("LOG: Fila de sessão " + accessCode + " vazia. Nenhuma música para tocar.");
            return Optional.empty();
        }

        QueueItem nextItem = fairOrder.get(0);

        // Remove the queue item from the session (entity relationship handling)
        session.deleteQueueItem(nextItem);
        sessionRepository.save(session);

        filaService.notificarAtualizacaoFila(accessCode);

        System.out.println("LOG: Próxima música (" + nextItem.getSong().getTitle() + ") selecionada e removida da fila da sessão " + accessCode);

        return Optional.of(nextItem);
    }
}