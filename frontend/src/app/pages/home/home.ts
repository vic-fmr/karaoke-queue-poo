import {Component} from '@angular/core';
import {CommonModule} from '@angular/common';
import {ReactiveFormsModule, FormControl, Validators} from '@angular/forms';
import {Router, RouterLink} from '@angular/router';
import {KaraokeService} from '../../services/KaraokeService';
import {AuthService} from '../../services/AuthService';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './home.html',
  styleUrls: ['./home.css'],
})
export class Home {
  sessionId = new FormControl('', [Validators.required]);
  error: string | null = null;
  loading: boolean = false;
  user: string = ''

  constructor(private router: Router, private ks: KaraokeService, private authService: AuthService) {


    const currentUser = this.authService.getCurrentUser();

    console.log(currentUser);

    if (currentUser) {
      this.user = currentUser.name;
    } else {
      this.user = 'Convidado';
    }
  }

  enter() {
    this.error = null;
    this.loading = true;

    const id = (this.sessionId.value || '').toString().trim();
    if (!id) {
      this.error = 'Informe o ID da sessão.';
      this.loading = false;
      return;
    }

    this.ks.getSession(id).subscribe({
      next: (valid) => {
        if (valid) {
          this.router.navigate(['/session', id]);
        } else {
          this.error = 'Sessão não encontrada.';
        }
        this.loading = false;
      },
      error: (err) => {
        console.log("Erro ao buscar sessão:", err);
        this.error = 'Sessão não encontrada.';
        this.loading = false;
      }
    });


  }

  createSession() {
    this.error = null;
    this.loading = true;

    // Ensure user is authenticated
    if (!this.authService.isAuthenticated()) {
      this.loading = false;
      // Redirect to login
      this.router.navigate(['/login']);
      return;
    }

    this.ks.createSession().subscribe({
      next: (session) => {
        // navigate to the newly created session
        this.loading = false;
        this.router.navigate(['/session', session.accessCode]);
      },
      error: (err) => {
        console.error('Erro ao criar sessão:', err);
        this.error = 'Não foi possível criar a sessão.';
        this.loading = false;
      }
    });
  }

  protected readonly name = name;
}
