package com.karaoke.backend.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FilaUpdateDTO {
    
    // Lista completa dos itens da fila (já mapeados para o formato leve QueueItemDTO)
    private List<QueueItemDTO> songQueue;
    
    // Opcional: Informações da Música Trocando Agora (História 6)
    // Para cumprir a História 6 ("ver qual música está tocando agora") [cite: 113]
    private QueueItemDTO nowPlaying;
    
    // Opcional: Status da Sessão
    private String sessionStatus; 
}