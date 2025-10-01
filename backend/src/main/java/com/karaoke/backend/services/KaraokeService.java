package com.karaoke.backend.services;

import com.karaoke.backend.models.KaraokeSession;
import com.karaoke.backend.models.QueueItem;
import com.karaoke.backend.models.Song;
import com.karaoke.backend.models.User;
import com.karaoke.backend.repositories.KaraokeSessionRepository;
import com.karaoke.backend.repositories.SongRepository;
import com.karaoke.backend.repositories.UserRepository;
import com.karaoke.backend.services.exception.SessionNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class KaraokeService {

    @Autowired
    private KaraokeSessionRepository sessionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SongRepository songRepository;

    @Transactional
    public KaraokeSession createSession() {
        KaraokeSession newSession = new KaraokeSession();
        KaraokeSession savedSession = sessionRepository.save(newSession);
        System.out.println("LOG: Nova sessão criada! Codigo de Acesso: " + savedSession.getAccessCode());
        return savedSession;
    }

    @Transactional
    public List<KaraokeSession> getAllSessions(){
        return sessionRepository.findAll();
    }

    @Transactional(readOnly = true)
    public KaraokeSession getSession(String accessCode) {
        return sessionRepository.findByAccessCode(accessCode.toUpperCase())
                .orElseThrow(() -> new SessionNotFoundException("Sessão com código '" + accessCode + "' não encontrada."));
    }

    @Transactional
    public void endSession(String accessCode) {
        KaraokeSession session = getSession(accessCode);
        sessionRepository.delete(session);
        System.out.println("LOG: Sessão finalizada: " + accessCode);
    }

    @Transactional
    public void addSongToQueue(String accessCode, String youtubeUrl, String userId, String userName) {
        KaraokeSession session = getSession(accessCode);


        User user = userRepository.findById(userId).orElseGet(() -> {
            User newUser = new User(userId, userName);
            newUser.setSession(session); // Associa o novo usuário à sessão
            return userRepository.save(newUser);
        });
        Song song = new Song(UUID.randomUUID().toString(), "Titulo da música (do YouTube)", "Artista (do YouTube)");

        songRepository.save(song);

        QueueItem queueItem = new QueueItem(UUID.randomUUID().toString(), user, song);
        session.addQueueItem(queueItem);
        sessionRepository.save(session);
        System.out.println("LOG: Música adicionada à fila da sessão " + accessCode);
    }
}