import { Component, OnDestroy, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { KaraokeService, Song, AddSongRequest } from '../../services/KaraokeService';
import { WebSocketService } from '../../services/WebSocketService';
import { AuthService } from '../../services/AuthService';

// Interface para representar um usuário conectado
interface ConnectedUser {
  id: string;
  name: string;
}

@Component({
  selector: 'app-session',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './session.html',
  styleUrls: ['./session.css'],
})
export class Session implements OnInit, OnDestroy {
  sessionCode: string | null = null;
  userName: string = '';
  userId: string = '';
  connectedUsers = signal<ConnectedUser[]>([]);

  queue: Song[] = [];
  current: Song | null = null;

  urlToAdd = '';
  addError: string | null = null;

  private subscriptions = new Subscription();

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private karaokeService: KaraokeService,
    private webSocketService: WebSocketService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    // Obtém o usuário autenticado
    const currentUser = this.authService.getCurrentUser();
    if (!currentUser) {
      // Se não houver usuário autenticado, redireciona para login
      this.router.navigate(['/login']);
      return;
    }

    this.userId = currentUser.id;
    this.userName = currentUser.name;

    this.sessionCode = this.route.snapshot.paramMap.get('id');
    if (!this.sessionCode) {
      this.addError = 'Código da sessão não encontrado!';
      return;
    }

    // Inicializa com o usuário atual
    this.connectedUsers.set([{ id: this.userId, name: this.userName }]);

    // 1. Busca o estado inicial da sessão via REST
    const initialLoadSub = this.karaokeService.getSession(this.sessionCode).subscribe({
      next: (session) => this.updateSessionState(session),
      error: (err) => {
        console.error('Erro ao carregar sessão', err);
        this.addError = 'Não foi possível carregar a sessão.';
      },
    });
    this.subscriptions.add(initialLoadSub);

    // 2. Conecta ao WebSocket e se inscreve para atualizações em tempo real
    this.webSocketService.connect(this.sessionCode);
    const wsSub = this.webSocketService.sessionUpdates$.subscribe((sessionState) => {
      console.log('Recebida atualização via WebSocket:', sessionState);
      this.updateSessionState(sessionState);
    });
    this.subscriptions.add(wsSub);
  }

  private updateSessionState(session: { queue: Song[]; currentSong: Song | null }) {
    this.queue = session.queue;
    this.current = session.currentSong;
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
    this.webSocketService.disconnect();
  }

  addSong() {
    this.addError = null;
    const songTitle = (this.urlToAdd || '').trim();
    if (!songTitle || !this.sessionCode) {
      this.addError = 'Título da música ou código da sessão inválido.';
      return;
    }

    const request: AddSongRequest = { songTitle, userId: this.userId, userName: this.userName };

    const addSub = this.karaokeService.addSong(this.sessionCode, request).subscribe({
      next: () => {
        this.urlToAdd = '';
      },
      error: (err) => {
        this.addError = err?.error || 'Erro ao adicionar música.';
        console.error(err);
      },
    });
    this.subscriptions.add(addSub);
  }

  removeSong(songId: string) {
    if (!this.sessionCode) return;

    const removeSub = this.karaokeService.removeSong(this.sessionCode, songId).subscribe({
      error: (err) => {
        console.error('Erro ao remover música', err);
      },
    });
    this.subscriptions.add(removeSub);
  }

  isAddedByMe(song: Song): boolean {
    return song.adicionadoPor === this.userName;
  }

  get pendingQueue(): Song[] {
    if (!this.current) return this.queue;
    return this.queue.filter((s) => s.id !== this.current?.id);
  }
}
