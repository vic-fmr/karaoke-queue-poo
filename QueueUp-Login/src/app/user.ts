import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class User { // <-- O nome da classe é 'User' (como o seu)

  private userName: string = '';
  private userHandle: string = '';

  constructor() { }

  // Salva o nome do usuário (usado no cadastro)
  registerUser(name: string) {
    this.userName = name;
    // Cria um "handle" simples a partir do nome
    this.userHandle = '@' + name.split(' ')[0].toLowerCase();
  }

  // Busca o nome (usado no perfil)
  getUserName(): string {
    return this.userName || 'Visitante'; // Nome padrão
  }

  // Busca o handle (usado no perfil)
  getUserHandle(): string {
    return this.userHandle || '@visitante'; // Handle padrão
  }
}
