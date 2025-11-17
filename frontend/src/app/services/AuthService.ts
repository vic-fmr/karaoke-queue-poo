import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, BehaviorSubject, tap } from 'rxjs';

export interface UserInfo {
  id: string;
  name: string;
  email: string;
}

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private base = 'http://localhost:8080/auth';
  private currentUserSubject = new BehaviorSubject<UserInfo | null>(this.getUserFromToken());
  constructor(private http: HttpClient, private router: Router) {}

  login(email: string, password: string): Observable<{ token: string }> {
    return this.http.post<{ token: string }>(`${this.base}/login`, { email, password }).pipe(
      tap((res) => {
        if (res?.token) localStorage.setItem('jwt', res.token);
      })
    );
  }

  register(username: string, email: string, password: string) {
    return this.http.post(`${this.base}/register`, { username, email, password });
  }

  logout() {
    localStorage.removeItem('jwt');
    this.router.navigate(['/login']);
  }

  getToken(): string | null {
    return localStorage.getItem('jwt');
  }

  isAuthenticated(): boolean {
    return !!this.getToken();
  }

  getCurrentUser(): UserInfo | null {
    return this.currentUserSubject.value;
  }

  private decodeToken(token: string): UserInfo | null {
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));

      // Log para debug - veja o que está no payload
      console.log('Token Payload:', payload);

      return {
        id: payload.userId || payload.id,
        name: payload.sub || payload.username || 'Usuário Desconhecido',
        email: payload.email || '',
      };
    } catch (e) {
      console.error('Erro ao decodificar token', e);
      return null;
    }
  }

  private getUserFromToken(): UserInfo | null {
    const token = this.getToken();
    if (!token) return null;
    return this.decodeToken(token);
  }
}
