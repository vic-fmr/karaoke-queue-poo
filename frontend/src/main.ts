import { bootstrapApplication } from '@angular/platform-browser';
import { appConfig } from './app/app.config';
import { App } from './app/app'; // <--- Deve ser 'App' e não 'AppComponent'
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { AuthInterceptor } from './app/interceptors/auth.interceptor';

bootstrapApplication(App, {
  providers: [
    provideHttpClient(withInterceptors([AuthInterceptor])),
    // ... outros providers ...
  ]
})
  .catch((err) => console.error(err));