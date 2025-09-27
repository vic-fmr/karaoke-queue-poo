/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.karaoke.backend.services;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.karaoke.backend.models.QueueItem;
import com.karaoke.backend.models.Song;
import com.karaoke.backend.models.User;
import com.karaoke.backend.services.exception.SessionNotFoundException;
import org.hibernate.validator.internal.constraintvalidators.hv.UUIDValidator;
import org.springframework.stereotype.Service;

import com.karaoke.backend.models.KaraokeSession;

@Service
public class KaraokeService {

    private final Map<String, KaraokeSession> activeSessions = new ConcurrentHashMap<>();

    //Tava dando erro
//    private final UUIDValidator uUIDValidator;
//
//    public KaraokeService(UUIDValidator uUIDValidator) {
//        // Se você precisar de outros serviços no futuro, adicione-os aqui, mas não o UUIDValidator
//        this.uUIDValidator = uUIDValidator;
//    }

    public KaraokeSession createSession() {
        KaraokeSession newSession = new KaraokeSession();
        activeSessions.put(newSession.getSessionId(), newSession);
        System.out.println("LOG: Nova sessão criada com o código: " + newSession.getSessionId());
        return newSession;
    }

    public KaraokeSession getSession(String sessionCode) {
        KaraokeSession session = activeSessions.get(sessionCode);

        if (session == null) {
            throw new SessionNotFoundException("Sessão com código '" + sessionCode + "' não encontrada.");
        }

        System.out.println("LOG: Sessão encontrada: " + sessionCode);
        return session;
    }

    public void endSession(String sessionCode) {
        KaraokeSession session = activeSessions.remove(sessionCode);
        if (session == null) {
            throw new SessionNotFoundException("Não foi possível finalizar a sessão com código '" + sessionCode + "' pois ela não foi encontrada.");
        }
        System.out.println("LOG: Sessão finalizada: " + sessionCode);
    }

    // Esse service ta simplificado.
    public void addSongToQueue(String sessionCode, String youtubeUrl, String userId){
        KaraokeSession session = getSession(sessionCode);

        //Refinar essas instancias
        User user = session.getConnectedUsers().computeIfAbsent(userId, id -> new User("Nome do Usuário", id));
        Song song = new Song("Titulo da música", youtubeUrl);

        QueueItem queueItem = new QueueItem(user, song);
        session.enqueueSong(queueItem);

        System.out.println("LOG: Música adicionada à fila da sessão " + sessionCode);
    }


}
