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
    private final TokenService tokenService;
    private final AuthenticationManager authenticationManager;

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(@RequestBody RegisterRequestDTO request) {
        authService.register(request);

        var usernamePassword = new UsernamePasswordAuthenticationToken(request.email(), request.password());
        var auth = authenticationManager.authenticate(usernamePassword);
        var token = tokenService.generateToken((User) auth.getPrincipal());
        return ResponseEntity.status(HttpStatus.CREATED).body(new AuthResponseDTO(token));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@RequestBody LoginRequestDTO request) {
        var usernamePassword = new UsernamePasswordAuthenticationToken(request.email(), request.password());
        var auth = authenticationManager.authenticate(usernamePassword);
        var token = tokenService.generateToken((User) auth.getPrincipal());
        return ResponseEntity.ok(new AuthResponseDTO(token));
    }
}