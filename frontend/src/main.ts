// TypeScript
// File: frontend/src/main.ts
import { bootstrapApplication } from '@angular/platform-browser';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptorsFromDi, HTTP_INTERCEPTORS } from '@angular/common/http';
import { AppComponent } from './app/app.component';
import { routes } from './app/app.routes';
import { AuthInterceptor } from './app/interceptors/auth.interceptor';

console.log('[main.ts] Iniciando bootstrap...');

bootstrapApplication(AppComponent, {
  providers: [
    provideRouter(routes),
    provideHttpClient(withInterceptorsFromDi()),
    { provide: HTTP_INTERCEPTORS, useClass: AuthInterceptor, multi: true }
  ]
})
  .then(() => console.log('[main.ts] Bootstrap concluído com sucesso'))
  .catch((err) => {
    console.error('[main.ts] Erro no bootstrap:', err);
    try {
      document.body.innerHTML = `<h1 style="color:red;">Erro: ${err?.message || String(err)}</h1>`;
    } catch {}
  });
