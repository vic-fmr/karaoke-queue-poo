import { Routes } from '@angular/router';
import { LoginComponent } from './login/login'; 
import { RegisterComponent } from './register/register'; 
import { ProfileComponent } from './profile/profile'; 

export const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'profile', component: ProfileComponent }, 

  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { path: '**', redirectTo: 'login' } 
];