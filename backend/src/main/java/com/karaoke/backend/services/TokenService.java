package com.karaoke.backend.services;


import com.karaoke.backend.models.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.io.Decoders;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Service
public class TokenService {

    @Value("${jwt.secret-key}")
    private String secret; // Sua chave secreta do application.properties

    @Value("${jwt.expiration}")
    private long expirationTime; // Tempo de expiração em milissegundos

    private Key getSigningKey() {
        // Decodifica a chave secreta de Base64 para bytes
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // 1. Geração do Token
    public String generateToken(User user) {
        // Data de expiração
        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + expirationTime);

        return Jwts.builder()
                .setSubject(user.getEmail())
                .claim("id", user.getId())
                .claim("name", user.getUsername())
                .setIssuedAt(now) // Data de emissão
                .setExpiration(expirationDate) // Data de expiração
                .signWith(getSigningKey(), SignatureAlgorithm.HS256) // Assina com a chave e algoritmo
                .compact(); // Constrói e serializa o token
    }

    // 2. Validação do Token (Retorna o Subject/Username)
    public String validateToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            // Retorna o email se a validação for bem-sucedida e o token não estiver expirado
            return claims.getSubject();

        } catch (Exception e) {
            // Se houver qualquer erro (token expirado, inválido, assinatura errada, etc.)
            // A exceção é capturada, e retornamos uma string vazia para indicar falha
            return "";
        }
    }
}