package com.karaoke.backend.repositories;

import com.karaoke.backend.models.KaraokeSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface KaraokeSessionRepository extends JpaRepository<KaraokeSession, Long> {

    // Spring Data JPA entende o nome do metodo e cria a query automaticamente!
    // "Encontre uma KaraokeSession pelo seu campo accessCode"
    Optional<KaraokeSession> findByAccessCode(String accessCode);
}