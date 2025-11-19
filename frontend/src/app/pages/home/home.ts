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
  user: string = '';

  constructor(
    private router: Router, 
    private ks: KaraokeService, 
    private authService: AuthService
  ) {
    const currentUser = this.authService.getCurrentUser();
    console.log(currentUser);

    if (currentUser) {
      this.user = currentUser.name;
    } else {
      this.user = 'Convidado';
    }
  }

  // Método para entrar em sessão existente
  enter() {
    this.error = null;
    
    const id = (this.sessionId.value || '').toString().trim();
    if (!id) {
      this.error = 'Informe o ID da sessão.';
      return;
    }
    
    this.loading = true;

    this.ks.getSession(id).subscribe({
      next: (session) => {
        // Se o backend retornar a sessão, significa que ela existe/é válida
        if (session) {
          this.router.navigate(['/session', id]);
        } else {
          this.error = 'Sessão não encontrada.';
        }
        this.loading = false;
      },
      error: (err) => {
        console.log("Erro ao buscar sessão:", err);
        this.error = 'Sessão não encontrada ou erro de conexão.';
        this.loading = false;
      }
    });
  }

  // Método implementado para CRIAR nova sessão
  createSession() {
    this.error = null;
    this.loading = true;

    this.ks.createSession().subscribe({
      next: (newSession) => {
        console.log('Sessão criada com sucesso:', newSession);
        this.loading = false;
        // Redireciona para a tela da sessão usando o código gerado pelo backend
        this.router.navigate(['/host', newSession.accessCode]);
      },
      error: (err) => {
        console.error('Erro ao criar sessão:', err);
        this.error = 'Erro ao criar nova sessão. Tente novamente.';
        this.loading = false;
      }
    });
  }
}