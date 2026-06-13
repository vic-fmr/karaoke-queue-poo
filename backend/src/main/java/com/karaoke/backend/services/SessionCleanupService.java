package com.karaoke.backend.services;

import com.karaoke.backend.repositories.KaraokeSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class SessionCleanupService {

    private final KaraokeSessionRepository sessionRepository;

    /**
     * Limpa sessões antigas a cada hora.
     * Consideramos "antiga" uma sessão que não teve atividade ou foi criada há mais de 6 horas.
     * Nota: Como não temos um campo 'lastActivity', usaremos o ID/Lógica de tempo se disponível, 
     * ou implementaremos um campo de criação. 
     * Por enquanto, vamos focar na lógica de expiração básica.
     */
    @Scheduled(cron = "0 0 * * * *") // Roda no início de cada hora
    @Transactional
    public void cleanupOldSessions() {
        log.info("Iniciando limpeza de sessões órfãs ou expiradas...");
        
        LocalDateTime limit = LocalDateTime.now().minusHours(6);
        
        sessionRepository.findAll().stream()
                .filter(s -> s.getCreatedAt() != null && s.getCreatedAt().isBefore(limit))
                .forEach(s -> {
                    log.info("Removendo sessão expirada (>6h): {}", s.getAccessCode());
                    sessionRepository.delete(s);
                });

        // Limpeza secundária: sessões vazias sem atividade (pode ser mais agressiva se quiser)
        sessionRepository.findAll().stream()
                .filter(s -> s.getConnectedUsers().isEmpty() && s.getSongQueue().isEmpty())
                .filter(s -> s.getCreatedAt().isBefore(LocalDateTime.now().minusHours(1)))
                .forEach(s -> {
                    log.info("Removendo sessão vazia inativa: {}", s.getAccessCode());
                    sessionRepository.delete(s);
                });
    }
}