package com.karaoke.backend.repositories;

import com.karaoke.backend.models.KaraokeSession;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class KaraokeSessionRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private KaraokeSessionRepository repository;

    @Test
    void findByAccessCode_ShouldReturnSession_WhenCodeExists() {
        KaraokeSession session = new KaraokeSession();
        String expectedCode = session.getAccessCode();
        entityManager.persistAndFlush(session);

        Optional<KaraokeSession> foundSessionOpt = repository.findByAccessCode(expectedCode);

        assertThat(foundSessionOpt).isPresent();
        assertThat(foundSessionOpt.get().getAccessCode()).isEqualTo(expectedCode);
    }

    @Test
    void findByAccessCode_ShouldReturnEmpty_WhenCodeDoesNotExist() {

        Optional<KaraokeSession> foundSessionOpt = repository.findByAccessCode("NONEXISTENT");

        assertThat(foundSessionOpt).isNotPresent();
    }

    @Test
    void save_ShouldPersistSessionAndGenerateId() {

        KaraokeSession newSession = new KaraokeSession();
        String code = newSession.getAccessCode();

        KaraokeSession savedSession = repository.save(newSession);

        assertThat(savedSession).isNotNull();
        assertThat(savedSession.getId()).isNotNull();
        assertThat(savedSession.getAccessCode()).isEqualTo(code);

        Optional<KaraokeSession> foundById = repository.findById(savedSession.getId());
        assertThat(foundById).isPresent();
        assertThat(foundById.get().getAccessCode()).isEqualTo(code);
    }
}
