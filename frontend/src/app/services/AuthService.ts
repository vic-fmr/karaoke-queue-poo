import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, BehaviorSubject, tap } from 'rxjs';
import { environment } from '../enviroments/environment';

export interface UserInfo {
  id: number;
  name: string;
  email: string;
}

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private base = environment.apiUrl + '/auth';
  // Expõe o Subject como Observable público para componentes
  public currentUser$ = new BehaviorSubject<UserInfo | null>(this.getUserFromToken()); 

  constructor(private http: HttpClient, private router: Router) {}

  login(email: string, password: string): Observable<{ token: string }> {
    return this.http.post<{ token: string }>(`${this.base}/login`, { email, password }).pipe(
      tap((res) => {
        if (res?.token) {
          localStorage.setItem('jwt', res.token);
          // CHAVE DA CORREÇÃO: Atualiza o Subject IMEDIATAMENTE após o login
          this.updateCurrentUser(res.token); 
        }
      })
    );
  }

  register(username: string, email: string, password: string) {
    return this.http.post(`${this.base}/register`, { username, email, password });
  }

  logout() {
    localStorage.removeItem('jwt');
    // Atualiza o Subject para null ao deslogar
    this.currentUser$.next(null);
    this.router.navigate(['/login']);
  }

  getToken(): string | null {
    return localStorage.getItem('jwt');
  }

  isAuthenticated(): boolean {
    return !!this.getToken();
  }

  // Este método agora usa o valor atual do Subject
  getCurrentUser(): UserInfo | null {
    return this.currentUser$.value; 
  }
  
  // Novo método para encapsular a atualização do estado
  private updateCurrentUser(token: string): void {
    const user = this.decodeToken(token);
    this.currentUser$.next(user);
  }

  private decodeToken(token: string): UserInfo | null {
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));

      // Log para debug - veja o que está no payload
      console.log('Token Payload:', payload);

      return {
        id: payload.userId || payload.id,
        name: payload.name || 'Usuário Desconhecido',
        email: payload.sub || '',
      };
    } catch (e) {
      console.error('Erro ao decodificar token', e);
      return null;
    }
  }

  // Inicializa o BehaviorSubject com o token existente
  private getUserFromToken(): UserInfo | null {
    const token = this.getToken();
    if (!token) return null;
    return this.decodeToken(token);
  }
}