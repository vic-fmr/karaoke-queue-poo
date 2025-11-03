package com.karaoke.backend.services;

// Imports corretos
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import com.karaoke.backend.dtos.FilaUpdateDTO;
import com.karaoke.backend.models.KaraokeSession;
import com.karaoke.backend.models.QueueItem;
import com.karaoke.backend.models.Song;
import com.karaoke.backend.models.User;
import com.karaoke.backend.repositories.KaraokeSessionRepository;

@ExtendWith(MockitoExtension.class)
class FilaServiceTest {

    @Mock
    private SimpMessagingTemplate template;

    @Mock
    private KaraokeSessionRepository sessionRepository;

    @InjectMocks
    private FilaService filaService;

    private KaraokeSession mockSession;
    private QueueItem item1;
    private QueueItem item2;

    @BeforeEach
    void setUp() {
        mockSession = new KaraokeSession();
        mockSession.setAccessCode("TEST1");
        mockSession.setStatus(KaraokeSession.SessionStatus.PLAYING);

        User user1 = new User("user1", "João");
        // CORRIGIDO: Adicionando o youtubeVideoId (o segundo argumento)
        Song song1 = new Song("song1", "YOUTUBE_ID_1", "Musica 1", "Artista 1");
        item1 = new QueueItem("q1", user1, song1);

        User user2 = new User("user2", "Maria");
        // CORRIGIDO: Adicionando o youtubeVideoId (o segundo argumento)
        Song song2 = new Song("song2", "YOUTUBE_ID_2", "Musica 2", "Artista 2");
        item2 = new QueueItem("q2", user2, song2);

        List<QueueItem> songQueue = new ArrayList<>(List.of(item1, item2));
        mockSession.setSongQueue(songQueue);
        
    }


    @Test
    void notificarAtualizacaoFila_ShouldSendUpdateToWebSocket() {
        String accessCode = "TEST1";
        
        when(sessionRepository.findByAccessCode(accessCode)).thenReturn(Optional.of(mockSession));
        
        ArgumentCaptor<FilaUpdateDTO> captor = ArgumentCaptor.forClass(FilaUpdateDTO.class);

        filaService.notificarAtualizacaoFila(accessCode);

        verify(template, times(1)).convertAndSend(
                eq("/topic/fila/" + accessCode), 
                captor.capture() 
        );

        FilaUpdateDTO sentDTO = captor.getValue();
        assertNotNull(sentDTO);
        
        assertEquals("PLAYING", sentDTO.getSessionStatus());
        assertEquals(2, sentDTO.getSongQueue().size());
        assertNotNull(sentDTO.getNowPlaying()); 
        
        assertEquals("q1", sentDTO.getNowPlaying().getQueueItemId()); 

        assertEquals("q2", sentDTO.getSongQueue().get(1).getQueueItemId());
    }

    @Test
    void notificarAtualizacaoFila_ShouldHandleEmptyQueue() {
        String accessCode = "EMPTY";
        mockSession.setAccessCode(accessCode);
        mockSession.setSongQueue(new ArrayList<>());
        
        when(sessionRepository.findByAccessCode(accessCode)).thenReturn(Optional.of(mockSession));
        ArgumentCaptor<FilaUpdateDTO> captor = ArgumentCaptor.forClass(FilaUpdateDTO.class);

        filaService.notificarAtualizacaoFila(accessCode);

        verify(template, times(1)).convertAndSend(
                eq("/topic/fila/" + accessCode),
                captor.capture()
        );

        FilaUpdateDTO sentDTO = captor.getValue();
        assertNotNull(sentDTO);
        
        assertEquals(0, sentDTO.getSongQueue().size());
        assertNull(sentDTO.getNowPlaying());
    }

    @Test
    void notificarAtualizacaoFila_ShouldThrowException_WhenSessionNotFound() {
        String accessCode = "BADCODE";
        when(sessionRepository.findByAccessCode(accessCode)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            filaService.notificarAtualizacaoFila(accessCode);
        });

        assertEquals("Sessão não encontrada.", exception.getMessage());
    }
}