package com.karaoke.backend.services;

import com.karaoke.backend.models.KaraokeSession;
import com.karaoke.backend.models.QueueItem;
import com.karaoke.backend.models.Song;
import com.karaoke.backend.models.User;
import com.karaoke.backend.repositories.KaraokeSessionRepository;
import com.karaoke.backend.repositories.QueueItemRepository;
import com.karaoke.backend.repositories.SongRepository;
import com.karaoke.backend.repositories.UserRepository;
import com.karaoke.backend.services.exception.SessionNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import static org.mockito.ArgumentMatchers.eq; 

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

        KaraokeSession result = karaokeService.getSession("test1"); // Testa toUpperCase()

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
        User newUser = new User();
        newUser.setUsername("João");
        Song newSong = new Song("song1", "Musica", "Artista");

        Long userId = 1L; 
        String userName = "João";
        String accessCode = "TEST1";
        String youtubeUrl = "youtube.com/url";
        
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

        karaokeService.addSongToQueue(accessCode, youtubeUrl, String.valueOf(userId), userName); 

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
        String youtubeUrl = "youtube.com/other";
        String userName = "Maria";

        when(sessionRepository.findByAccessCode(accessCode)).thenReturn(Optional.of(session));
        when(userRepository.findById(eq(existingUserId))).thenReturn(Optional.of(existingUser)); 

        karaokeService.addSongToQueue(accessCode, youtubeUrl, String.valueOf(existingUserId), userName);

        verify(userRepository, times(1)).findById(eq(existingUserId)); 
        verify(userRepository, never()).save(any(User.class));
        verify(filaService, times(1)).notificarAtualizacaoFila(accessCode);
    }
    
    @Test
    void deleteSongFromQueue_ShouldRemoveItemAndNotify() {
        KaraokeSession session = new KaraokeSession();
        User mockUser = new User();
        mockUser.setUsername("Temp User");
        Song mockSong = new Song("tempSongId", "Temp Song", "Temp Artist");
        
        String queueItemId = "q1"; 
        QueueItem item = new QueueItem(queueItemId, mockUser, mockSong); 
        
        String accessCode = "TEST1";

        when(sessionRepository.findByAccessCode(accessCode)).thenReturn(Optional.of(session));
        when(queueItemRepository.findById(queueItemId)).thenReturn(Optional.of(item)); 
        doNothing().when(queueItemRepository).delete(item);
        doNothing().when(filaService).notificarAtualizacaoFila(accessCode);

        karaokeService.deleteSongFromQueue(accessCode, queueItemId);

        verify(queueItemRepository, times(1)).findById(queueItemId);
        verify(queueItemRepository, times(1)).delete(item); // Verifica se o delete foi chamado
        verify(filaService, times(1)).notificarAtualizacaoFila(accessCode);
    }
}

