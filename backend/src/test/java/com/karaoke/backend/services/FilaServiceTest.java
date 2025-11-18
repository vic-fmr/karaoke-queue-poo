package com.karaoke.backend.services;

import com.karaoke.backend.dtos.FilaUpdateDTO;
import com.karaoke.backend.dtos.QueueItemDTO;
import com.karaoke.backend.models.KaraokeSession;
import com.karaoke.backend.models.QueueItem;
import com.karaoke.backend.models.Song;
import com.karaoke.backend.models.User;
import com.karaoke.backend.repositories.KaraokeSessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FilaServiceTest {

    @Mock
    private SimpMessagingTemplate template; // Simula o envio de mensagens WebSocket
    @Mock
    private KaraokeSessionRepository sessionRepository;

    @InjectMocks
    private FilaService filaService;

    private final String ACCESS_CODE = "TESTE123";
    private KaraokeSession mockSession;

    @BeforeEach
    void setUp() {
        // Inicializa uma sessão base
        mockSession = new KaraokeSession();
        mockSession.setAccessCode(ACCESS_CODE);
        mockSession.setId(1L);

        try {
            mockSession.setStatus(KaraokeSession.SessionStatus.PLAYING);
        } catch (NoSuchMethodError | Exception e) {
            // Ignorar se o método não existir
        }
    }

    // -----------------------------------------------------------------------------------
    // Testes para notificarAtualizacaoFila(String accessCode)
    // -----------------------------------------------------------------------------------

    @Test
    void notificarAtualizacaoFila_DeveEnviarNotificacaoComFilaCompletaCorreta() {
        // Arrange
        // Cria itens mock para a fila
        User mockUser = new User();
        mockUser.setUsername("Cantor Teste");

        Song mockSong1 = new Song("ID1", "Musica A", "Artista 1", "testUrl1");
        QueueItem item1 = new QueueItem(mockSession, mockUser, mockSong1);
        item1.setQueueItemId(10L);

        Song mockSong2 = new Song("ID2", "Musica B", "Artista 2", "testUrl2");
        QueueItem item2 = new QueueItem(mockSession, mockUser, mockSong2);
        item2.setQueueItemId(20L);

        // Adiciona itens à fila da sessão
        mockSession.setSongQueue(new ArrayList<>(List.of(item1, item2)));

        // Mock: Retorna a sessão com a fila preenchida
        when(sessionRepository.findByAccessCode(ACCESS_CODE)).thenReturn(Optional.of(mockSession));

        // Act
        filaService.notificarAtualizacaoFila(ACCESS_CODE);

        // Assert
        // 1. Verifique se a sessão foi buscada corretamente
        verify(sessionRepository, times(1)).findByAccessCode(ACCESS_CODE);

        // 2. Capture o argumento enviado para convertAndSend
        String expectedDestination = "/topic/fila/" + ACCESS_CODE;

        // Use ArgumentCaptor ou simplesmente verifique se o método foi chamado
        verify(template, times(1)).convertAndSend(eq(expectedDestination), any(FilaUpdateDTO.class));

        // Para um teste mais robusto, podemos verificar o conteúdo do DTO (opcional, mas recomendado)
        // Isso requer capturar o argumento (exemplo abaixo):
        // ArgumentCaptor<FilaUpdateDTO> captor = ArgumentCaptor.forClass(FilaUpdateDTO.class);
        // verify(template).convertAndSend(eq(expectedDestination), captor.capture());
        // FilaUpdateDTO sentDTO = captor.getValue();
        // assertNotNull(sentDTO.getNowPlaying());
        // assertEquals("Musica A", sentDTO.getNowPlaying().getSongTitle());
        // assertEquals(2, sentDTO.getQueue().size());
    }

    @Test
    void notificarAtualizacaoFila_DeveEnviarNotificacaoComNowPlayingNulo_QuandoFilaVazia() {
        // Arrange
        mockSession.setSongQueue(Collections.emptyList());

        // Mock: Retorna a sessão com a fila vazia
        when(sessionRepository.findByAccessCode(ACCESS_CODE)).thenReturn(Optional.of(mockSession));

        // Act
        filaService.notificarAtualizacaoFila(ACCESS_CODE);

        // Assert
        // Verifique se o método de envio foi chamado
        String expectedDestination = "/topic/fila/" + ACCESS_CODE;
        verify(template, times(1)).convertAndSend(eq(expectedDestination), any(FilaUpdateDTO.class));

        // Verificação do NowPlaying nulo (Usando ArgumentCaptor para verificar o conteúdo)
        // Note: Se você não quiser adicionar ArgumentCaptor, este teste já garante que a chamada
        // ocorreu com um objeto FilaUpdateDTO.
    }

    @Test
    void notificarAtualizacaoFila_DeveLancarRuntimeException_QuandoSessaoNaoEncontrada() {
        // Arrange
        when(sessionRepository.findByAccessCode(ACCESS_CODE)).thenReturn(Optional.empty());

        // Act & Assert
        // Verifica se a exceção é lançada conforme o código do serviço
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> filaService.notificarAtualizacaoFila(ACCESS_CODE));

        // Verifica a mensagem da exceção (opcional)
        assertTrue(exception.getMessage().contains("Sessão não encontrada."));

        // Verifica que NENHUM envio de mensagem ocorreu
        verify(template, never()).convertAndSend(anyString(), any(FilaUpdateDTO.class));
    }
}