import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { KaraokeService } from '../services/KaraokeService';
import { map, catchError } from 'rxjs/operators';
import { of } from 'rxjs';

export const sessionGuard: CanActivateFn = (route) => {
  const router = inject(Router);
  const ks = inject(KaraokeService);
  const id = route.paramMap.get('id') || '';

  if (!id) {
    router.navigate(['/']);
    return false;
  }

  return ks.getSession(id).pipe(
    map((session) => {
      // Se a sessão existe (retorna um objeto válido), permite acesso
      if (session && session.accessCode) {
        return true;
      }
      // Caso contrário, redireciona
      router.navigate(['/']);
      return false;
    }),
    catchError((err) => {
      // Em caso de erro (ex: sessão não encontrada - 404), redireciona
      console.error('Erro ao verificar sessão:', err);
      router.navigate(['/']);
      return of(false);
    })
  );
};
