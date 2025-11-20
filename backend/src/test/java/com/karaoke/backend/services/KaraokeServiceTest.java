package com.karaoke.backend.services;

import com.karaoke.backend.dtos.YouTubeVideoDTO;
import com.karaoke.backend.exception.SessionNotFoundException;
import com.karaoke.backend.models.KaraokeSession;
import com.karaoke.backend.models.QueueItem;
import com.karaoke.backend.models.Song;
import com.karaoke.backend.models.User;
import com.karaoke.backend.repositories.KaraokeSessionRepository;
import com.karaoke.backend.repositories.QueueItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KaraokeServiceTest {

    @Mock
    private KaraokeSessionRepository sessionRepository;
    @Mock
    private FilaService filaService;
    @Mock
    private QueueItemRepository queueItemRepository;
    @Mock
    private YoutubeService youTubeService;
    @Mock
    private SongService songService;
    @Mock
    private com.karaoke.backend.repositories.UserRepository userRepository;

    @InjectMocks
    private KaraokeService karaokeService;

    // Dados de teste comuns
    private KaraokeSession mockSession;
    private final String ACCESS_CODE = "ABCD12";

    @BeforeEach
    void setUp() {
        // Inicializa um mock de sessão para uso em vários testes
        mockSession = new KaraokeSession();
        mockSession.setId(1L);
        mockSession.setAccessCode(ACCESS_CODE);
    }

    // -----------------------------------------------------------------------------------
    // Testes para createSession()
    // -----------------------------------------------------------------------------------

    @Test
    void createSession_DeveCriarESalvarUmaNovaSessao() {
        // Arrange
        // Simula o salvamento e retorno de uma sessão com um ID/código gerado
        when(sessionRepository.save(any(KaraokeSession.class))).thenReturn(mockSession);

        // Act
        KaraokeSession result = karaokeService.createSession();

        // Assert
        assertNotNull(result);
        assertEquals(mockSession.getId(), result.getId());
        // Verifica se o método save do repository foi chamado exatamente uma vez
        verify(sessionRepository, times(1)).save(any(KaraokeSession.class));
    }

    // -----------------------------------------------------------------------------------
    // Testes para getAllSessions()
    // -----------------------------------------------------------------------------------

    @Test
    void getAllSessions_DeveRetornarTodasAsSessoes() {
        // Arrange
        List<KaraokeSession> expectedSessions = List.of(mockSession, new KaraokeSession());
        when(sessionRepository.findAll()).thenReturn(expectedSessions);

        // Act
        List<KaraokeSession> result = karaokeService.getAllSessions();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(expectedSessions, result);
        verify(sessionRepository, times(1)).findAll();
    }

    // -----------------------------------------------------------------------------------
    // Testes para getSession(String accessCode)
    // -----------------------------------------------------------------------------------

    @Test
    void getSession_DeveRetornarSessao_QuandoEncontrada() {
        // Arrange
        when(sessionRepository.findByAccessCode(ACCESS_CODE)).thenReturn(Optional.of(mockSession));

        // Act
        KaraokeSession result = karaokeService.getSession(ACCESS_CODE.toLowerCase()); // Testa a conversão para maiúsculas

        // Assert
        assertNotNull(result);
        assertEquals(ACCESS_CODE, result.getAccessCode());
        // Verifica se a busca foi feita com o código em MAIÚSCULAS
        verify(sessionRepository, times(1)).findByAccessCode(ACCESS_CODE);
    }

    @Test
    void getSession_DeveLancarSessionNotFoundException_QuandoNaoEncontrada() {
        // Arrange
        when(sessionRepository.findByAccessCode(ACCESS_CODE)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(SessionNotFoundException.class, () -> karaokeService.getSession(ACCESS_CODE));
        verify(sessionRepository, times(1)).findByAccessCode(ACCESS_CODE);
    }

    // -----------------------------------------------------------------------------------
    // Testes para endSession(String accessCode)
    // -----------------------------------------------------------------------------------

    @Test
    void endSession_DeveExcluirSessao_QuandoEncontrada() {
        // Arrange
        when(sessionRepository.findByAccessCode(ACCESS_CODE)).thenReturn(Optional.of(mockSession));

        // Act
        karaokeService.endSession(ACCESS_CODE);

        // Assert
        // Verifica se a sessão foi buscada
        verify(sessionRepository, times(1)).findByAccessCode(ACCESS_CODE);
        // Verifica se o método delete foi chamado com a sessão correta
        verify(sessionRepository, times(1)).delete(mockSession);
    }

    @Test
    void endSession_DeveLancarSessionNotFoundException_QuandoNaoEncontrada() {
        // Arrange
        when(sessionRepository.findByAccessCode(ACCESS_CODE)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(SessionNotFoundException.class, () -> karaokeService.endSession(ACCESS_CODE));
        // Verifica que o delete NUNCA foi chamado
        verify(sessionRepository, never()).delete(any(KaraokeSession.class));
    }

    // -----------------------------------------------------------------------------------
    // Testes para addSongToQueue(String accessCode, AddSongRequestDTO request, User user)
    // -----------------------------------------------------------------------------------

    @Test
    void addSongToQueue_DeveAdicionarMusicaAFilaComSucesso() {
        // Arrange
        String videoId = "V123";
        String title = "Bohemian Rhapsody";
        String thumbnailUrl = "http://thumb.url";
        
        // NOVO: Usamos o DTO completo
        YouTubeVideoDTO requestDTO = new YouTubeVideoDTO(videoId, title, thumbnailUrl, true);
        
        User mockUser = new User();
        mockUser.setId(10L);
        
        // A lógica de busca do YouTube e criação da música agora é mais simples, pois
        // o DTO já carrega os dados.
        Song mockSong = new Song();
        mockSong.setTitle(title);
        mockSong.setYoutubeVideoId(videoId);

        // 1. Mock do getSession()
        when(sessionRepository.findByAccessCode(ACCESS_CODE)).thenReturn(Optional.of(mockSession));
        
        
        // 3. Mock do salvamento da sessão (para verificar a chamada)
        when(sessionRepository.save(any(KaraokeSession.class))).thenReturn(mockSession);

        // Act
        karaokeService.addSongToQueue(ACCESS_CODE, requestDTO, mockUser); // NOVO: Passando requestDTO

        // Assert
        // Verifica se a sessão foi buscada
        verify(sessionRepository, times(1)).findByAccessCode(ACCESS_CODE);
                
        // Verifica se o usuário foi adicionado à sessão 
        assertTrue(mockSession.getConnectedUsers().contains(mockUser));
        
        // Verifica se o item foi adicionado à fila
        assertEquals(1, mockSession.getSongQueue().size());
        
        // Verifica se a sessão foi salva
        verify(sessionRepository, times(1)).save(mockSession);
        
        // Verifica se a notificação foi enviada
        verify(filaService, times(1)).notificarAtualizacaoFila(ACCESS_CODE);
    }

    @Test
    void addSongToQueue_DeveLancarSessionNotFoundException_QuandoSessaoNaoEncontrada() {
        // Arrange
        // NOVO: Criação do DTO
        YouTubeVideoDTO requestDTO = new YouTubeVideoDTO("V1", "Title", "url", true);
        User mockUser = new User();

        // 1. Mock do getSession()
        when(sessionRepository.findByAccessCode(ACCESS_CODE)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(SessionNotFoundException.class,
                () -> karaokeService.addSongToQueue(ACCESS_CODE, requestDTO, mockUser)); // NOVO: Passando requestDTO

        // Assertions de verificação
        verify(sessionRepository, times(1)).findByAccessCode(ACCESS_CODE);
        
        // Verifica que NENHUMA outra operação aconteceu
        verify(sessionRepository, never()).save(any());
        verify(filaService, never()).notificarAtualizacaoFila(anyString());
    }

    // O teste para VideoNotFoundException não é mais necessário aqui, pois
    // a verificação de vídeo válido foi movida para o front/serviço de busca,
    // e o KaraokeService assume que o DTO contém dados válidos de um vídeo selecionado.
    // O teste anterior era:
    /*
    @Test
    void addSongToQueue_DeveLancarVideoNotFoundException_QuandoNenhumVideoValidoEncontrado() {
        // ... (Removido, pois a lógica de busca/validação não está mais neste método)
    }
    */

    // -----------------------------------------------------------------------------------
    // Testes para deleteSongFromQueue(String accessCode, Long queueItemId)
    // -----------------------------------------------------------------------------------

    @Test
    void deleteSongFromQueue_DeveRemoverItemDaFila_QuandoExistente() {
        // Arrange
        Long queueItemId = 99L;
        QueueItem mockItem = new QueueItem(mockSession, new User(), new Song());
        mockItem.setQueueItemId(queueItemId);

        // Garante que a sessão contenha o item para simular a lógica
        mockSession.addQueueItem(mockItem);

        // 1. Mock do getSession()
        when(sessionRepository.findByAccessCode(ACCESS_CODE)).thenReturn(Optional.of(mockSession));
        // 2. Mock da busca do QueueItem
        when(queueItemRepository.findById(queueItemId)).thenReturn(Optional.of(mockItem));

        // Act
        karaokeService.deleteSongFromQueue(ACCESS_CODE, queueItemId);

        // Assert
        // Verifica que o item foi removido da lista da sessão (a lógica está no método deleteQueueItem)
        assertFalse(mockSession.getSongQueue().contains(mockItem));
        // Verifica se a notificação foi enviada (que é chamada no final, independentemente da exclusão)
        verify(filaService, times(1)).notificarAtualizacaoFila(ACCESS_CODE);
        // O item é removido implicitamente quando a sessão é salva novamente, mas o seu serviço
        // não tem um `sessionRepository.save()`. Como o método é `@Transactional`,
        // a exclusão na coleção `session.deleteQueueItem(itemToDelete);` será persistida.
        // O mais importante é verificar a notificação e a manipulação da coleção.
    }

    @Test
    void deleteSongFromQueue_NaoDeveRemoverNemLancarErro_QuandoItemNaoExistente() {
        // Arrange
        Long nonExistingId = 999L;

        // 1. Mock do getSession()
        when(sessionRepository.findByAccessCode(ACCESS_CODE)).thenReturn(Optional.of(mockSession));
        // 2. Mock da busca do QueueItem (retorna vazio)
        when(queueItemRepository.findById(nonExistingId)).thenReturn(Optional.empty());

        // Act
        karaokeService.deleteSongFromQueue(ACCESS_CODE, nonExistingId);

        // Assert
        // Verifica que a notificação foi enviada (que é chamada no final)
        verify(filaService, times(1)).notificarAtualizacaoFila(ACCESS_CODE);
        // Verifica que o método do repositório foi chamado
        verify(queueItemRepository, times(1)).findById(nonExistingId);
        // A lógica de remoção interna da sessão não será chamada
        // O teste deve garantir que NENHUM erro foi lançado
    }

    @Test
    void deleteSongFromQueue_DeveLancarSessionNotFoundException_QuandoSessaoNaoEncontrada() {
        // Arrange
        Long queueItemId = 99L;

        // 1. Mock do getSession() (lança exceção)
        when(sessionRepository.findByAccessCode(ACCESS_CODE)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(SessionNotFoundException.class,
                () -> karaokeService.deleteSongFromQueue(ACCESS_CODE, queueItemId));

        // Assertions de verificação: Se a sessão não for encontrada, o resto não é chamado
        verify(queueItemRepository, never()).findById(anyLong());
        verify(filaService, never()).notificarAtualizacaoFila(anyString());
    }
}