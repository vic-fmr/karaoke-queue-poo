package com.karaoke.backend.services;

import com.karaoke.backend.models.KaraokeSession;
import com.karaoke.backend.models.User;
import com.karaoke.backend.repositories.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;

    // MELHORIA: Injeção de Dependência via Construtor e campo final
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Converte o userId (String) para Long.
     * Tenta buscar o usuário pelo ID. Se não encontrar, cria um novo usuário,
     * associa-o à sessão e o salva.
     *
     * @throws IllegalArgumentException se o userId não for um número válido.
     */
    @Transactional
    public User getOrCreateUser(String userId, String userName, KaraokeSession session) {

        // 1. Lógica de Validação e Conversão de Entrada
        Long userIdAsLong;
        try {
            userIdAsLong = Long.parseLong(userId);
        } catch (NumberFormatException e) {
            // A exceção é lançada aqui, limpando o KaraokeService
            throw new IllegalArgumentException("ID do usuário inválido: " + userId);
        }

        // 2. Busca ou Cria o usuário (Lógica de Persistência)
        return userRepository.findById(userIdAsLong).orElseGet(() -> {
            User newUser = new User();
            newUser.setUsername(userName);
            session.addUser(newUser);
            return newUser;
        });
    }
}