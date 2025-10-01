package com.karaoke.backend.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "app_user") // Usamos "app_user" porque "user" pode ser uma palavra reservada no SQL
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    private String userId; // Mantemos o UUID como ID, já que é gerado na aplicação

    private String name;

    @ManyToOne(fetch = FetchType.LAZY) // 1. Define um relacionamento "Muitos para Um": Muitos usuários pertencem a uma sessão
    @JoinColumn(name = "session_id") // 2. Cria uma coluna "session_id" na tabela "app_user" para a chave estrangeira
    @JsonIgnore // 3. Evita que a sessão seja serializada junto com o usuário, prevenindo loops infinitos
    private KaraokeSession session;

    public User(String userId, String name) {
        this.userId = userId;
        this.name = name;
    }
}