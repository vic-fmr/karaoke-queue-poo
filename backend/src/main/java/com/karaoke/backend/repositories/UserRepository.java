package com.karaoke.backend.repositories;

import com.karaoke.backend.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, String> { // O ID do User é String (UUID)
}