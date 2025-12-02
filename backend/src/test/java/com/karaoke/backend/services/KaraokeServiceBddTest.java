package com.karaoke.backend.services;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.karaoke.backend.dtos.YouTubeVideoDTO;
import com.karaoke.backend.models.KaraokeSession;
import com.karaoke.backend.models.User;
import com.karaoke.backend.repositories.KaraokeSessionRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("BDD: Funcionalidade de Adicionar Música")
class KaraokeServiceBddTest {

    @Mock
    private KaraokeSessionRepository sessionRepository;

    @Mock
    private FilaService filaService;

    @InjectMocks
    private KaraokeService karaokeService;

    @Test
    @DisplayName("Cenário: Adição de música com sucesso em uma sessão ativa")
    void deveAdicionarMusicaComSucesso() {
        // --- DADO (Given) ---
        String accessCode = "KARA01";
        
        // Mock da Sessão existente
        KaraokeSession session = new KaraokeSession();
        session.setAccessCode(accessCode);
        session.setId(1L);

        // Mock do Usuário
        User user = new User();
        user.setId(10L);
        user.setUsername("João");

        // Mock da Requisição (O vídeo selecionado)
        YouTubeVideoDTO request = new YouTubeVideoDTO(
            "Video123",          // videoId
            "Evidências",        // title
            "http://thumb.jpg",   // thumbnail
            true
        );

        // Configuração do comportamento do Mock
        when(sessionRepository.findByAccessCode(accessCode)).thenReturn(Optional.of(session));
        when(sessionRepository.save(any(KaraokeSession.class))).thenReturn(session);

        // --- QUANDO (When) ---
        karaokeService.addSongToQueue(accessCode, request, user);

        // --- ENTÃO (Then) ---
        
        // 1. A música deve ser registrada na lista de reprodução (validação de estado)
        assertFalse(session.getSongQueue().isEmpty(), "A fila não deve estar vazia");
        assertEquals("Evidências", session.getSongQueue().get(0).getSong().getTitle(), "O título da música deve corresponder");
        assertEquals("João", session.getSongQueue().get(0).getUser().getUsername(), "O usuário dono da música deve ser João");

        // 2. A sessão deve ser salva (validação de interação)
        verify(sessionRepository, times(1)).save(session);

        // 3. O sistema deve notificar via WebSocket (validação de interação)
        verify(filaService, times(1)).notificarAtualizacaoFila(accessCode);
    }
}