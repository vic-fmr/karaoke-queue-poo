package com.karaoke.backend.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.karaoke.backend.dtos.AddSongRequestDTO;
import com.karaoke.backend.models.KaraokeSession;
import com.karaoke.backend.models.QueueItem;
import com.karaoke.backend.models.Song;
import com.karaoke.backend.models.User;
import com.karaoke.backend.repositories.KaraokeSessionRepository;
import com.karaoke.backend.repositories.QueueItemRepository;
import com.karaoke.backend.repositories.SongRepository;
import com.karaoke.backend.repositories.UserRepository;

import jakarta.servlet.ServletException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class KaraokeControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private KaraokeSessionRepository sessionRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private SongRepository songRepository;
    @Autowired
    private QueueItemRepository queueItemRepository;

    private final String BASE_URL = "/api/sessions";
    private KaraokeSession testSession;
    private User testUser;

    @BeforeEach
    @Transactional
    void setUp() {
        queueItemRepository.deleteAll();
        songRepository.deleteAll();
        userRepository.deleteAll();
        sessionRepository.deleteAll();

        testSession = sessionRepository.save(new KaraokeSession());

        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setPassword("password");
        testUser.setEmail("test@test.com");
        testUser = userRepository.save(testUser);
    }


    @Test
    @WithMockUser
    @Transactional
    void createSession_ShouldReturn201AndSessionData() throws Exception {
        long countBefore = sessionRepository.count();

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.accessCode", matchesPattern("[A-Z0-9]{6}")))
                .andExpect(jsonPath("$.status").value("WAITING"));

        assertThat(sessionRepository.count()).isEqualTo(countBefore + 1);
    }

    @Test
    @Transactional
    void createSession_WithoutAuth_ShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    @Transactional
    void getAllSessions_ShouldReturnListOfSessions() throws Exception {
        sessionRepository.save(new KaraokeSession());

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].accessCode").value(testSession.getAccessCode()));
    }

    @Test
    @WithMockUser
    @Transactional
    void getSession_WhenExists_ShouldReturnSession() throws Exception {
        String accessCode = testSession.getAccessCode();

        mockMvc.perform(get(BASE_URL + "/" + accessCode.toLowerCase()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testSession.getId()))
                .andExpect(jsonPath("$.accessCode").value(accessCode))
                .andExpect(jsonPath("$.status").value("WAITING"));
    }

    @Test
    @WithMockUser
    @Transactional
    void getSession_WhenNotExists_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(get(BASE_URL + "/INVALID"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Sessão com código 'INVALID' não encontrada."));
    }

    @Test
    @WithMockUser
    @Transactional
    void addSongToQueue_ShouldAddSongAndReturnOk() throws Exception {
        String accessCode = testSession.getAccessCode();
        long userCountBefore = userRepository.count();
        long songCountBefore = songRepository.count();

        AddSongRequestDTO requestDTO = new AddSongRequestDTO();
        requestDTO.setYoutubeUrl("youtube.com/test");
        requestDTO.setUserId(String.valueOf(testUser.getId()));
        requestDTO.setUserName(testUser.getUsername());

        mockMvc.perform(post(BASE_URL + "/" + accessCode.toUpperCase() + "/queue")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk());

        KaraokeSession updatedSession = sessionRepository.findByAccessCode(accessCode).orElseThrow();
        assertThat(updatedSession.getSongQueue()).hasSize(1);
        assertThat(updatedSession.getSongQueue().get(0).getUser().getId()).isEqualTo(testUser.getId());
        assertThat(updatedSession.getSongQueue().get(0).getSong()).isNotNull();
        assertThat(userRepository.count()).isEqualTo(userCountBefore);
        assertThat(songRepository.count()).isEqualTo(songCountBefore + 1);
        assertThat(queueItemRepository.count()).isEqualTo(1);
    }

    @Test
    @WithMockUser
    @Transactional
    void addSongToQueue_WithNewUser_ShouldCreateUserAndAddSong() throws Exception {
        String accessCode = testSession.getAccessCode();
        long userCountBefore = userRepository.count();
        long queueCountBefore = queueItemRepository.count();

        String newUserIdString = "999";
        String newUserName = "New User";

        AddSongRequestDTO requestDTO = new AddSongRequestDTO();
        requestDTO.setYoutubeUrl("youtube.com/new");
        requestDTO.setUserId(newUserIdString);
        requestDTO.setUserName(newUserName);

        mockMvc.perform(post(BASE_URL + "/" + accessCode.toUpperCase() + "/queue")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk());

        KaraokeSession updatedSession = sessionRepository.findByAccessCode(accessCode).orElseThrow();
        assertThat(userRepository.count()).isEqualTo(userCountBefore + 1);
        assertThat(queueItemRepository.count()).isEqualTo(queueCountBefore + 1);
        assertThat(updatedSession.getSongQueue()).hasSize(1);
        assertThat(updatedSession.getSongQueue().get(0).getUser().getUsername()).isEqualTo(newUserName);

        User createdUser = userRepository.findByUsername(newUserName).orElseThrow(() -> new AssertionError("Novo usuário não encontrado pelo username"));
        assertThat(createdUser.getUsername()).isEqualTo(newUserName);
         // Opcional: Verificar se o ID é o esperado, pode ser útil se formos controlar a sequência H2
        // assertThat(createdUser.getId()).isEqualTo(Long.parseLong(newUserIdString));
    }

@Test
    @WithMockUser
    @Transactional
    void addSongToQueue_WithInvalidUserIdFormat_ShouldCauseIllegalArgumentException() { 
         String accessCode = testSession.getAccessCode();

         AddSongRequestDTO requestDTO = new AddSongRequestDTO();
         requestDTO.setYoutubeUrl("youtube.com/bad");
         requestDTO.setUserId("not-a-number");
         requestDTO.setUserName("Bad User");

         ServletException exception = assertThrows(ServletException.class, () -> {
             mockMvc.perform(post(BASE_URL + "/" + accessCode + "/queue")
                             .contentType(MediaType.APPLICATION_JSON)
                             .content(objectMapper.writeValueAsString(requestDTO)));
         });

         Throwable rootCause = exception.getRootCause();
         assertNotNull(rootCause, "A causa raiz da ServletException não deveria ser nula");
         assertTrue(rootCause instanceof IllegalArgumentException, "A causa raiz deveria ser IllegalArgumentException");
         assertEquals("ID do usuário inválido: not-a-number", rootCause.getMessage(), "Mensagem da exceção incorreta");
    }


    @Test
    @WithMockUser
    @Transactional
    void endSession_ShouldDeleteSessionAndReturnNoContent() throws Exception {
        String accessCode = testSession.getAccessCode();
        long countBefore = sessionRepository.count();

        mockMvc.perform(delete(BASE_URL + "/" + accessCode.toUpperCase()))
                .andExpect(status().isNoContent());

        assertThat(sessionRepository.findByAccessCode(accessCode)).isNotPresent();
        assertThat(sessionRepository.count()).isEqualTo(countBefore - 1);
    }


    @Test
    @WithMockUser
    @Transactional
    void deleteSongFromQueue_ShouldDeleteItemAndReturnNoContent() throws Exception {
        String accessCode = testSession.getAccessCode();

        Song song = songRepository.save(new Song(java.util.UUID.randomUUID().toString(), "To Delete", "Artist"));
        QueueItem item = queueItemRepository.save(new QueueItem(java.util.UUID.randomUUID().toString(), testUser, song));
        testSession.addQueueItem(item);
        sessionRepository.save(testSession);

        String queueItemId = item.getQueueItemId();
        long queueCountBefore = queueItemRepository.count();

        mockMvc.perform(delete(BASE_URL + "/" + accessCode.toUpperCase() + "/queue/" + queueItemId))
                .andExpect(status().isNoContent());

        assertThat(queueItemRepository.findById(queueItemId)).isNotPresent();
        assertThat(queueItemRepository.count()).isEqualTo(queueCountBefore - 1);
        assertThat(sessionRepository.findByAccessCode(accessCode)).isPresent();
    }
}

