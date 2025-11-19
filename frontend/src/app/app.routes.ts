import {Routes} from '@angular/router';
import {Login} from './pages/login/login';
import {Register} from './pages/register/register';
// import {Profile} from './pages/profile/profile';
import {Home} from './pages/home/home';
import {Session} from './pages/session/session';
import {sessionGuard} from './guards/session.guard';
import {WelcomePage} from './pages/welcome-page/welcome-page';
import { Host } from './pages/host/host';

export const routes: Routes = [
  {path: '', component: WelcomePage},
  {path: 'home', component: Home},
  {path: 'login', component: Login},
  {path: 'register', component: Register},
  // { path: 'profile', component: Profile },
  {path: 'session/:id', component: Session, canActivate: [sessionGuard]},
  {path: 'host/:id', component: Host, canActivate: [sessionGuard]},

  {path: '**', redirectTo: ''},
];
