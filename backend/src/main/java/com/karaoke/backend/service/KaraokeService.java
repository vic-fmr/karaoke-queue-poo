/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.karaoke.backend.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.karaoke.backend.model.KaraokeSession;

@Service
public class KaraokeService {

    private final Map<String, KaraokeSession> activeSessions = new ConcurrentHashMap<>();
    
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

}
