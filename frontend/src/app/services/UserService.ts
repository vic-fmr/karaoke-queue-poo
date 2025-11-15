import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class UserService {

  private userName: string = '';
  private userHandle: string = '';

  constructor() { }

  registerUser(name: string) {
    this.userName = name;
    // Cria um "handle" simples a partir do nome
    this.userHandle = '@' + name.split(' ')[0].toLowerCase();
  }

  getUserName(): string {
    return this.userName || 'Visitante'; // Nome padrão
  }

  getUserHandle(): string {
    return this.userHandle || '@visitante'; // Handle padrão
  }
}
