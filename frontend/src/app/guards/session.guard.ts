import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { KaraokeService } from '../services/KaraokeService';
import { map } from 'rxjs/operators';

export const sessionGuard: CanActivateFn = (route) => {
  const router = inject(Router);
  const ks = inject(KaraokeService);
  const id = route.paramMap.get('id') || '';

  if (!id) {
    router.navigate(['/']);
    return false;
  }
  
  return ks.validateSession(id).pipe(
    map(valid => {
      if (!valid) router.navigate(['/']);
      return valid;
    })
  );
};

