package com.karaoke.backend.repositories;

import com.karaoke.backend.models.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository repository;

    @Test
    void findByUsername_ShouldReturnUser_WhenUserExists() {
        User testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@email.com");
        testUser.setPassword("hashedpassword");
        entityManager.persistAndFlush(testUser);

        Optional<User> foundUserOpt = repository.findByUsername("testuser");

        assertThat(foundUserOpt).isPresent();
        assertThat(foundUserOpt.get().getUsername()).isEqualTo("testuser");
        assertThat(foundUserOpt.get().getEmail()).isEqualTo("test@email.com");
        assertThat(foundUserOpt.get().getId()).isNotNull();
    }

    @Test
    void findByUsername_ShouldReturnEmpty_WhenUserDoesNotExist() {

        Optional<User> foundUserOpt = repository.findByUsername("nonexistent");

        assertThat(foundUserOpt).isNotPresent();
    }

    @Test
    void existsByUsername_ShouldReturnTrue_WhenUserExists() {
        User testUser = new User();
        testUser.setUsername("existing");
        testUser.setEmail("exist@email.com");
        testUser.setPassword("password");
        entityManager.persistAndFlush(testUser);

        boolean exists = repository.existsByUsername("existing");

        assertThat(exists).isTrue();
    }

    @Test
    void existsByUsername_ShouldReturnFalse_WhenUserDoesNotExist() {
        boolean exists = repository.existsByUsername("nonexistent");

        assertThat(exists).isFalse();
    }

     @Test
    void save_ShouldPersistUser() {
        User newUser = new User();
        newUser.setUsername("newuser");
        User savedUser = repository.save(newUser);  

        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getId()).isNotNull();

        Long generatedId = savedUser.getId(); 
        Optional<User> foundById = repository.findById(generatedId);
        assertThat(foundById).isPresent();
        assertThat(foundById.get().getUsername()).isEqualTo("newuser");
    }
}
