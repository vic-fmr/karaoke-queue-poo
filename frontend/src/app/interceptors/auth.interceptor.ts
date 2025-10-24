import { Injectable } from '@angular/core';
import { HttpEvent, HttpHandler, HttpInterceptor, HttpRequest } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {
  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    const token = localStorage.getItem('jwt');

    // 1. VERIFICAÇÃO CRÍTICA: Se a URL for para login ou registro, não adicione o token, mesmo que ele exista.
    // Isso garante que a requisição POST /auth/login seja anônima.
    if (req.url.includes('/auth/login') || req.url.includes('/auth/register')) {
      return next.handle(req);
    }

    // 2. Se não houver token (e a rota não for pública), segue com a requisição original.
    if (!token) {
      return next.handle(req);
    }

    // 3. Se houver token e a rota for privada, clona a requisição e adiciona o cabeçalho.
    const authReq = req.clone({
      setHeaders: { Authorization: `Bearer ${token}` }
    });

    return next.handle(authReq);
  }
}
