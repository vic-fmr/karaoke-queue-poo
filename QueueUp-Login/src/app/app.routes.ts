import { Routes } from '@angular/router';
import { Login } from './login/login';
import { Register } from './register/register';
import { Profile } from './profile/profile';

export const routes: Routes = [
  { path: 'login', component: Login },
  { path: 'register', component: Register },
  { path: 'profile', component: Profile },

  // Rotas padrão
  { path: '', redirectTo: 'login', pathMatch: 'full' }, // Abre em /login
  { path: '**', redirectTo: 'login' } // Qualquer outra coisa vai para /login
];
