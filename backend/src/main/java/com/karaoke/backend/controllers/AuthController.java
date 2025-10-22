package com.karaoke.backend.controllers;

import com.karaoke.backend.dtos.AuthResponseDTO;
import com.karaoke.backend.dtos.LoginRequestDTO;
import com.karaoke.backend.dtos.RegisterRequestDTO;
import com.karaoke.backend.models.User;
import com.karaoke.backend.services.AuthService;
import com.karaoke.backend.services.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final TokenService tokenService; // Novo
    private final AuthenticationManager authenticationManager;

    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody RegisterRequestDTO request) {
        User user = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    // O método /login virá depois da configuração do JWT e Security
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@RequestBody LoginRequestDTO request) {
        // 1. Cria o objeto de autenticação com as credenciais
        var usernamePassword = new UsernamePasswordAuthenticationToken(request.username(), request.password());

        // 2. Tenta autenticar (chama o UserDetailsService)
        var auth = authenticationManager.authenticate(usernamePassword);

        // 3. Se autenticado, gera o token
        var token = tokenService.generateToken((User) auth.getPrincipal());

        // 4. Retorna o token para o cliente
        return ResponseEntity.ok(new AuthResponseDTO(token));
    }
}
