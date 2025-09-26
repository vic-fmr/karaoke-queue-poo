/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.karaoke.backend.model;

import java.util.UUID;

public class User {

    private final String userId;

    private String name;

    private String codigoSessao;

    public String getName() {
        return name;
    }

    public String getUserId() {
        return userId;
    }

    public String getCodigoSessao() {
        return codigoSessao;
    }

    public User(){
        this.userId = UUID.randomUUID().toString();
    }

    public User(String nome, String codigoSessao) {
        this.userId = UUID.randomUUID().toString();
        this.name = nome;
        this.codigoSessao = codigoSessao;
    }

    public void setUserName(String nome) {
        this.name = nome;
    }

    @Override
    public String toString() {
        return "Usuario{" +
               "id='" + userId + '\'' +
               ", nome='" + name + '\'' +
               ", codigoSessao='" + codigoSessao + '\'' +
               '}';
    }

}
