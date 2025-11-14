import { Routes } from '@angular/router';
import { Login } from './login/login';
import { Register } from './register/register';
import { Profile } from './profile/profile';
import { HomeComponent } from './home/home';
import { SessionComponent } from './session/session';
import { sessionGuard } from './guards/session.guard';

export const routes: Routes = [
  { path: 'login', component: Login },
  { path: 'register', component: Register },
  { path: 'profile', component: Profile },

  // New routes for Karaoke app
  { path: '', component: HomeComponent },
  { path: 'session/:id', component: SessionComponent, canActivate: [sessionGuard] },

  // fallback to home
  { path: '**', redirectTo: '' }
];
