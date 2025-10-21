package com.karaoke.backend.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QueueItemDTO {

    // Informações da Música
    private String songTitle;
    private String youtubeLink; // Assumindo que a classe Song eventualmente armazenará o link/URL
    
    // Informações do Participante
    private String addedByUserName; 
    
    // Identificador único do item da fila, útil para o frontend na remoção (História 5)
    private String queueItemId; 
    
    // Opcional: Adicionar um timestamp simplificado se necessário
    // private String timeAdded; 

    // Método estático para mapeamento (melhor se implementado em uma classe Mapper ou Service)
    // Para simplificar, colocamos no DTO para demonstração
    public static QueueItemDTO fromEntity(com.karaoke.backend.models.QueueItem item) {
        // Seus modelos User e Song estão aninhados em QueueItem
        
        // **IMPORTANTE**: Você precisará garantir que sua entidade Song tenha o campo 'url'
        // Seu modelo Song atual: private String title; private String artist;
        // Se o link do YouTube for o principal identificador, ele deve ser adicionado ao modelo Song.
        
        return new QueueItemDTO(
            item.getSong().getTitle(),
            //item.getSong().getUrl(), // <<-- ASSUMINDO que você adicionou .getUrl() no Song
            "link_temporario_ate_ajustar_o_Song", 
            item.getUser().getName(),
            item.getQueueItemId()
        );
    }
}