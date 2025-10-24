package com.karaoke.backend.services;

import com.karaoke.backend.dtos.RegisterRequestDTO;
import com.karaoke.backend.models.User;
import com.karaoke.backend.repositories.UserRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor // Use Lombok para construtor (opcional, senão use @Autowired)
public class AuthService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // Lógica de Cadastro
    public User register(RegisterRequestDTO dto) {
        if (userRepository.existsByEmail(dto.email())) {
            throw new RuntimeException("Usuário já existe"); // Trate melhor essa exceção
        }

        String encodedPassword = passwordEncoder.encode(dto.password());

        User newUser = new User();
        newUser.setUsername(dto.username());
        newUser.setEmail(dto.email());
        newUser.setPassword(encodedPassword);

        return userRepository.save(newUser);
    }



    // A lógica de Login será tratada principalmente pelo Spring Security com JWT,
    // mas o serviço de JWT (TokenService) precisará ser injetado aqui.
    // Por enquanto, vamos focar no cadastro. O login virá na próxima etapa.
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + email));
    }

}