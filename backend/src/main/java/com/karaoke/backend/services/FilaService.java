package com.karaoke.backend.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

// Importe seus DTOs
import com.karaoke.backend.dtos.FilaUpdateDTO;
import com.karaoke.backend.dtos.QueueItemDTO;
import com.karaoke.backend.models.KaraokeSession;
import com.karaoke.backend.models.QueueItem;
import com.karaoke.backend.repositories.KaraokeSessionRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FilaService {

    @Autowired
    private SimpMessagingTemplate template;
    @Autowired
    private KaraokeSessionRepository sessionRepository; // Assumindo um Repository para obter a Sessão
    
    // Metodo que você chamará sempre que a fila mudar (Adicionar, Remover, Pular)
    public void notificarAtualizacaoFila(String accessCode) {
        
        // 1. Busque a sessão completa (com a lista de QueueItem já ordenada pelo @OrderBy no seu modelo)
        KaraokeSession session = sessionRepository.findByAccessCode(accessCode)
            .orElseThrow(() -> new RuntimeException("Sessão não encontrada."));
        
        // 2. Mapeie a lista de entidades para DTOs
        List<QueueItemDTO> dtoList = session.getSongQueue().stream()
            .map(QueueItemDTO::fromEntity)
            .collect(Collectors.toList());

        // 3. Crie o DTO de Envelope (FilaUpdateDTO)
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