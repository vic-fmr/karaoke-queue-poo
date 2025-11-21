import {Component, OnInit, OnDestroy} from '@angular/core';
import {CommonModule} from '@angular/common';
import {ReactiveFormsModule, FormControl, Validators} from '@angular/forms';
import {Router } from '@angular/router';
import {Subscription} from 'rxjs'; // Importação do Subscription
import {KaraokeService} from '../../services/KaraokeService';
import {AuthService, UserInfo} from '../../services/AuthService'; // Importação de UserInfo

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './home.html',
  styleUrls: ['./home.css'],
})
export class Home implements OnInit, OnDestroy {
  sessionId = new FormControl('', [Validators.required]);
  error: string | null = null;
  loading: boolean = false;
  user: string = '';

  private userSubscription: Subscription = new Subscription();
  private currentUser: UserInfo | null = null; // Armazena o objeto do usuário

  constructor(
    private router: Router, 
    private ks: KaraokeService, 
    private authService: AuthService
  ) {
    // A lógica de inicialização é movida para ngOnInit
  }

  ngOnInit(): void {
    // Se inscreve no Observable do usuário para ser notificado sobre mudanças (login/logout)
    this.userSubscription = this.authService.currentUser$.subscribe(user => {
      this.currentUser = user; // Atualiza o objeto completo
      if (user) {
        this.user = user.name; // Usa o nome real
      } else {
        this.user = 'Convidado'; // Define como Convidado se for nulo
      }
      console.log('Usuário no Home (Reativo):', this.user);
    });
  }

  ngOnDestroy(): void {
    // Garante que a subscrição seja cancelada para evitar vazamentos de memória
    this.userSubscription.unsubscribe();
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

    // Usa o objeto de usuário reativo para verificar a autenticação
    if (!this.currentUser) {
      this.loading = false;
      // Redireciona para login
      this.router.navigate(['/login']);
      return;
    }

    this.ks.createSession().subscribe({
      next: (newSession) => {
        console.log('Sessão criada com sucesso:', newSession);
        this.loading = false;
        // Redireciona para a tela do host
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