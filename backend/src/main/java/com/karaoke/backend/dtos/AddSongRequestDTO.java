package com.karaoke.backend.dtos;

public record AddSongRequestDTO(
    String videoId,      // ID do Youtube (ex: dQw4w9WgXcQ)
    String title,        // Título do vídeo
    String thumbnailUrl  // URL da capa
    // userId e userName você pode manter se ainda usar, 
    // mas vi que você pega o user via @AuthenticationPrincipal
) {}