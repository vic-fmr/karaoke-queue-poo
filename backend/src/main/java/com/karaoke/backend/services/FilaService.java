package com.karaoke.backend.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

// Importe seus DTOs
import com.karaoke.backend.dtos.FilaUpdateDTO;
import com.karaoke.backend.dtos.QueueItemDTO;
import com.karaoke.backend.models.KaraokeSession;
import com.karaoke.backend.repositories.KaraokeSessionRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FilaService {

    @Autowired
    private SimpMessagingTemplate template;
    @Autowired
    private KaraokeSessionRepository sessionRepository;

    public void notificarAtualizacaoFila(String accessCode) {

        // 1. Recupera a sessão do karaokê pelo accessCode
        KaraokeSession session = sessionRepository.findByAccessCode(accessCode)
            .orElseThrow(() -> new RuntimeException("Sessão não encontrada."));
        
        // 2. Mapeia a lista de entidades para DTOs
        List<QueueItemDTO> dtoList = session.getSongQueue().stream()
            .map(QueueItemDTO::fromEntity)
            .collect(Collectors.toList());

        // 3. Cria o DTO de Envelope (FilaUpdateDTO)
        // Lógica de "nowPlaying": Para começar, é o primeiro item da lista (ou null se vazia)
        QueueItemDTO nowPlayingDTO = dtoList.isEmpty() ? null : dtoList.get(0);

        FilaUpdateDTO filaAtualizada = new FilaUpdateDTO(
            dtoList,
            nowPlayingDTO,
            session.getStatus().name()
        );
        
        // 4. Envie a atualização para o tópico de WebSocket
        // Tópico: /topic/fila/{accessCode}
        String destination = "/topic/fila/" + accessCode;
        template.convertAndSend(destination, filaAtualizada);
    }
}