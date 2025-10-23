import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private base = '/auth';
  constructor(private http: HttpClient, private router: Router) {}

  login(username: string, password: string): Observable<{ token: string }> {
    return this.http.post<{ token: string }>(`${this.base}/login`, { username, password })
      .pipe(tap(res => {
        if (res?.token) localStorage.setItem('jwt', res.token);
      }));
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
}
