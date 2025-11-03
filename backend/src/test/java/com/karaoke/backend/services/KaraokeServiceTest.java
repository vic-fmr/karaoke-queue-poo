package com.karaoke.backend.services;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.karaoke.backend.dtos.YouTubeVideoDTO;
import com.karaoke.backend.models.KaraokeSession;
import com.karaoke.backend.models.QueueItem;
import com.karaoke.backend.models.Song;
import com.karaoke.backend.models.User;
import com.karaoke.backend.repositories.KaraokeSessionRepository;
import com.karaoke.backend.repositories.QueueItemRepository;
import com.karaoke.backend.repositories.SongRepository;
import com.karaoke.backend.repositories.UserRepository;
import com.karaoke.backend.services.exception.SessionNotFoundException;
import com.karaoke.backend.services.exception.VideoNotFoundException;

@ExtendWith(MockitoExtension.class)
class KaraokeServiceTest {

    @Mock
    private KaraokeSessionRepository sessionRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private SongRepository songRepository;
    @Mock
    private FilaService filaService;
    @Mock
    private QueueItemRepository queueItemRepository;
    @Mock
    private YouTubeService youTubeService; // <-- NOVO MOCK

    @InjectMocks
    private KaraokeService karaokeService;

    @Test
    void createSession_ShouldSaveAndReturnSession() {
        KaraokeSession session = new KaraokeSession();
        session.setAccessCode("MOCKCODE"); 
        
        when(sessionRepository.save(any(KaraokeSession.class))).thenReturn(session);

        KaraokeSession result = karaokeService.createSession();

        assertNotNull(result);
        assertNotNull(result.getAccessCode());
        verify(sessionRepository, times(1)).save(any(KaraokeSession.class));
    }

    @Test
    void getAllSessions_ShouldReturnListFromRepository() {
        List<KaraokeSession> mockList = List.of(new KaraokeSession(), new KaraokeSession());
        when(sessionRepository.findAll()).thenReturn(mockList);

        List<KaraokeSession> result = karaokeService.getAllSessions();

        assertEquals(2, result.size());
        verify(sessionRepository, times(1)).findAll();
    }

    @Test
    void getSession_ShouldReturnSession_WhenCodeIsValid() {
        KaraokeSession session = new KaraokeSession();
        when(sessionRepository.findByAccessCode("TEST1")).thenReturn(Optional.of(session));

        KaraokeSession result = karaokeService.getSession("test1"); 

        assertNotNull(result);
        verify(sessionRepository, times(1)).findByAccessCode("TEST1");
    }

    @Test
    void getSession_ShouldThrowException_WhenCodeIsInvalid() {
        when(sessionRepository.findByAccessCode("BADCODE")).thenReturn(Optional.empty());

        assertThrows(SessionNotFoundException.class, () -> {
            karaokeService.getSession("BADCODE");
        });
        verify(sessionRepository, times(1)).findByAccessCode("BADCODE");
    }

    @Test
    void addSongToQueue_ShouldCreateNewUser_WhenUserNotFound() {
        KaraokeSession session = new KaraokeSession();
        // CORRIGIDO: Song agora requer youtubeVideoId
        Song newSong = new Song("song1", "YOUTUBE_ID_1", "Música de Teste", "Artista"); 
        
        Long userId = 1L; 
        String userName = "João";
        String accessCode = "TEST1";
        final String SONG_TITLE = "Música de Teste"; // <-- ENTRADA DO USUÁRIO

        // Mock do YouTubeService (Simula sucesso na busca)
        YouTubeVideoDTO mockVideo = new YouTubeVideoDTO("YOUTUBE_ID_1", "Música de Teste", true);
        when(youTubeService.searchVideos(eq(SONG_TITLE + " karaoke"))).thenReturn(List.of(mockVideo));
        
        // Mocks de Persistência
        when(sessionRepository.findByAccessCode(accessCode)).thenReturn(Optional.of(session));
        when(userRepository.findById(eq(userId))).thenReturn(Optional.empty()); 
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User userToSave = invocation.getArgument(0);
            userToSave.setId(userId);
            return userToSave;
        });
        when(songRepository.save(any(Song.class))).thenReturn(newSong); 
        when(sessionRepository.save(session)).thenReturn(session);
        doNothing().when(filaService).notificarAtualizacaoFila(accessCode);

        // CORRIGIDO: Passando o SONG_TITLE
        karaokeService.addSongToQueue(accessCode, SONG_TITLE, String.valueOf(userId), userName); 

        verify(youTubeService, times(1)).searchVideos(eq(SONG_TITLE + " karaoke"));
        verify(userRepository, times(1)).findById(eq(userId)); 
        verify(userRepository, times(1)).save(any(User.class));
        verify(songRepository, times(1)).save(any(Song.class));
        verify(sessionRepository, times(1)).save(session);
        verify(filaService, times(1)).notificarAtualizacaoFila(accessCode);
    }
    
    @Test
    void addSongToQueue_ShouldUseExistingUser_WhenUserIsFound() {
        KaraokeSession session = new KaraokeSession();
        Long existingUserId = 2L;
        User existingUser = new User();
        existingUser.setId(existingUserId);
        existingUser.setUsername("Maria");
        
        String accessCode = "TEST2";
        final String SONG_TITLE = "Outra Música";
        String userName = "Maria";
        
        // Mock do YouTubeService
        YouTubeVideoDTO mockVideo = new YouTubeVideoDTO("YOUTUBE_ID_2", SONG_TITLE, true);
        when(youTubeService.searchVideos(eq(SONG_TITLE + " karaoke"))).thenReturn(List.of(mockVideo));

        // Mocks da Persistência
        when(sessionRepository.findByAccessCode(accessCode)).thenReturn(Optional.of(session));
        when(userRepository.findById(eq(existingUserId))).thenReturn(Optional.of(existingUser)); 
        when(songRepository.save(any(Song.class))).thenReturn(new Song()); // Necessário para simular o save
        when(sessionRepository.save(session)).thenReturn(session); // Necessário para simular o save
        doNothing().when(filaService).notificarAtualizacaoFila(accessCode);


        // CORRIGIDO: Passando o SONG_TITLE
        karaokeService.addSongToQueue(accessCode, SONG_TITLE, String.valueOf(existingUserId), userName);

        verify(youTubeService, times(1)).searchVideos(eq(SONG_TITLE + " karaoke"));
        verify(userRepository, times(1)).findById(eq(existingUserId)); 
        verify(userRepository, never()).save(any(User.class));
        verify(songRepository, times(1)).save(any(Song.class));
        verify(filaService, times(1)).notificarAtualizacaoFila(accessCode);
    }
    
    @Test
    void addSongToQueue_ShouldThrowVideoNotFoundException_WhenNoVideoIsFound() {
        KaraokeSession session = new KaraokeSession();
        String accessCode = "TEST3";
        String songTitle = "Musica Inexistente";
        
        // Mock: Simula o YouTubeService retornando uma lista vazia
        when(sessionRepository.findByAccessCode(accessCode)).thenReturn(Optional.of(session));
        when(youTubeService.searchVideos(eq(songTitle + " karaoke"))).thenReturn(List.of());

        // Verifica se a exceção é lançada
        assertThrows(VideoNotFoundException.class, () -> {
            karaokeService.addSongToQueue(accessCode, songTitle, "10", "Teste");
        });

        // Verificações: Garante que nada foi salvo
        verify(youTubeService, times(1)).searchVideos(anyString());
        verify(userRepository, never()).findById(anyLong()); 
        verify(songRepository, never()).save(any(Song.class));
        verify(sessionRepository, never()).save(any(KaraokeSession.class));
    }

    @Test
    void deleteSongFromQueue_ShouldRemoveItemAndNotify() {
        KaraokeSession session = new KaraokeSession();
        User mockUser = new User();
        mockUser.setUsername("Temp User");
        
        // CORRIGIDO: Song agora requer youtubeVideoId
        Song mockSong = new Song("tempSongId", "TEMP_YOUTUBE_ID", "Temp Song", "Temp Artist");
        
        String queueItemId = "q1"; 
        QueueItem item = new QueueItem(queueItemId, mockUser, mockSong); 
        
        String accessCode = "TEST1";

        when(sessionRepository.findByAccessCode(accessCode)).thenReturn(Optional.of(session));
        when(queueItemRepository.findById(queueItemId)).thenReturn(Optional.of(item)); 
        doNothing().when(queueItemRepository).delete(item);
        doNothing().when(filaService).notificarAtualizacaoFila(accessCode);

        karaokeService.deleteSongFromQueue(accessCode, queueItemId);

        verify(queueItemRepository, times(1)).findById(queueItemId);
        verify(queueItemRepository, times(1)).delete(item);
        verify(filaService, times(1)).notificarAtualizacaoFila(accessCode);
    }
}