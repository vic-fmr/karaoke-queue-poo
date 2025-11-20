package com.karaoke.backend.dtos;

import com.karaoke.backend.models.User;

public record UserDTO(
        Long id,
        String username
) {
    /**
     * Construtor estático para criar um UserDTO a partir de uma entidade User.
     * Isso evita expor dados sensíveis como senhas.
     * @param user A entidade User.
     * @return Um novo UserDTO.
     */
    public static UserDTO fromEntity(User user) {
        if (user == null) {
            return null;
        }
        return new UserDTO(user.getId(), user.getUsername());
    }
}
