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
import java.util.Map;
import java.util.Queue;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
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
    // 2. Calcula a fila justa (interleaving por usuário) para envio ao front
    List<com.karaoke.backend.models.QueueItem> fairOrdered = computeFairOrder(session);

    // 3. Mapeia a lista justa para DTOs
    List<QueueItemDTO> dtoList = fairOrdered.stream()
        .map(QueueItemDTO::fromEntity)
        .collect(Collectors.toList());

    // 4. Lógica de "nowPlaying": primeiro item da lista justa (ou null se vazia)
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

    /**
     * Calcula a ordem justa (round-robin por usuário) a partir do estado atual da sessão,
     * começando pelo usuário indicado por session.nextUserIndex e respeitando a ordem
     * definida em session.rotationUserIds. Usuários que tenham itens na fila mas não
     * estejam em rotationUserIds serão anexados ao final na ordem de primeira aparição.
     */
    public List<com.karaoke.backend.models.QueueItem> computeFairOrder(KaraokeSession session) {
        List<com.karaoke.backend.models.QueueItem> all = session.getSongQueue();

        // Agrupa por usuário mantendo ordem por timestamp (a lista original já é timestamp-ordenada)
        Map<String, Queue<com.karaoke.backend.models.QueueItem>> perUser = new HashMap<>();
        List<String> usersFirstAppearance = new ArrayList<>();
        for (com.karaoke.backend.models.QueueItem qi : all) {
            String uid = qi.getUser() != null && qi.getUser().getId() != null ? qi.getUser().getId().toString() : "";
            perUser.computeIfAbsent(uid, k -> new LinkedList<>()).add(qi);
            if (!usersFirstAppearance.contains(uid)) usersFirstAppearance.add(uid);
        }

        // Start with rotationUserIds order, but append any users present in queue but missing in rotationUserIds
        List<String> rotation = new ArrayList<>(session.getRotationUserIds());
        for (String uid : usersFirstAppearance) {
            if (!rotation.contains(uid)) rotation.add(uid);
        }

        List<com.karaoke.backend.models.QueueItem> result = new ArrayList<>();

        if (rotation.isEmpty()) return result;

        int idx = session.getNextUserIndex();
        if (idx < 0 || idx >= rotation.size()) idx = 0;

        boolean addedAny = true;
        while (addedAny) {
            addedAny = false;
            for (int i = 0; i < rotation.size(); i++) {
                int userPos = (idx + i) % rotation.size();
                String uid = rotation.get(userPos);
                Queue<com.karaoke.backend.models.QueueItem> q = perUser.get(uid);
                if (q != null && !q.isEmpty()) {
                    result.add(q.poll());
                    addedAny = true;
                }
            }
            // depois de completar uma volta, continue tentando até esvaziar tudo
        }

        return result;
    }
}