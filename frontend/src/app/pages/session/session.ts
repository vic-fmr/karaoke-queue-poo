import {Component, OnDestroy, OnInit, signal} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';
import {ActivatedRoute, Router, RouterLink} from '@angular/router';
import {Subscription} from 'rxjs';
import {KaraokeService, QueueItemEntity, KaraokeSession, AddSongRequest} from '../../services/KaraokeService';
import {WebSocketService, FilaUpdate, QueueItemDTO} from '../../services/WebSocketService';
import {AuthService} from '../../services/AuthService';

interface ConnectedUser {
  id: number;
  name: string;
}

// Modelo simplificado para a View, comum aos dois formatos (entidade e DTO)
interface QueueViewItem {
  id: number;               // queueItemId
  songTitle: string;        // song.title ou DTO.songTitle
  youtubeLink: string;      // song.url ou DTO.youtubeLink
  addedByUserName: string;  // user.username ou DTO.addedByUserName
}

@Component({
  selector: 'app-session',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './session.html',
  styleUrls: ['./session.css'],
})
export class Session implements OnInit, OnDestroy {
  sessionCode: string | null = null;
  userName: string = '';
  userId: number = 0;
  connectedUsers = signal<ConnectedUser[]>([]);
  isAddingSong: boolean = false;

  queue = signal<QueueViewItem[]>([]);
  current = signal<QueueViewItem | null>(null);

  urlToAdd = '';
  addError: string | null = null;

  private subscriptions = new Subscription();

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private karaokeService: KaraokeService,
    private webSocketService: WebSocketService,
    private authService: AuthService
  ) {
    console.log('Session component instantiated');
  }

  ngOnInit(): void {
    console.log('[Session] ngOnInit iniciado');

    const currentUser = this.authService.getCurrentUser();
    if (!currentUser) {
      this.router.navigate(['/login']);
      return;
    }

    this.userId = currentUser.id;
    this.userName = currentUser.name;

    this.sessionCode = this.route.snapshot.paramMap.get('id') || localStorage.getItem('currentSessionCode');

    console.log('[Session] SessionCode:', this.sessionCode);

    if (!this.sessionCode) {
      this.addError = 'Código da sessão não encontrado!';
      return;
    }

    localStorage.setItem('currentSessionCode', this.sessionCode);
    this.connectedUsers.set([{id: this.userId, name: this.userName}]);

    // 1) Estado inicial via REST (entidade KaraokeSession)
    console.log('[Session] Fazendo chamada getSession para:', this.sessionCode);
    const initialLoadSub = this.karaokeService.getSession(this.sessionCode).subscribe({
      next: (session: KaraokeSession) => {
        console.log('[Session] ✅ Resposta recebida do backend:', session);
        const mappedQueue = this.mapEntitiesToView(session.songQueue);
        this.queue.set(mappedQueue);
        // Não há nowPlaying no SessionResponse; ficará null até atualizar via WS ou quando backend incluir
        this.current.set(null);
        console.log('[Session] Queue signal após set:', this.queue());
      },
      error: (err) => {
        console.error('[Session] ❌ Erro ao carregar sessão:', err);
        this.addError = 'Não foi possível carregar a sessão.';
      },
      complete: () => {
        console.log('[Session] getSession completado');
      },
    });
    this.subscriptions.add(initialLoadSub);

    // 2) Conecta ao WebSocket para atualizações (FilaUpdateDTO)
    console.log('[Session] Conectando ao WebSocket');
    this.webSocketService.connect(this.sessionCode);

    const wsSub = this.webSocketService.filaUpdates$.subscribe((filaUpdate: FilaUpdate) => {
      console.log('[Session] 🔔 Recebida atualização via WebSocket:', filaUpdate);
      const mappedQueue = this.mapDtosToView(filaUpdate.songQueue);
      this.queue.set(mappedQueue);
      this.current.set(filaUpdate.nowPlaying ? this.mapDtoToView(filaUpdate.nowPlaying) : null);
    });

    this.subscriptions.add(wsSub);
    console.log('[Session] ngOnInit finalizado');
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
    this.webSocketService.disconnect();
  }

  addSong() {
    this.addError = null;
    this.isAddingSong = true;

    const songTitle = (this.urlToAdd || '').trim();
    if (!songTitle || !this.sessionCode) {
      this.addError = 'Título da música ou código da sessão inválido.';
      return;
    }

    const request: AddSongRequest = {songTitle};

    const addSub = this.karaokeService.addSong(this.sessionCode, request).subscribe({
      next: () => {
        this.urlToAdd = '';
        this.isAddingSong = false;
        console.log('[Session] Música adicionada com sucesso');
      },
      error: (err) => {
        this.addError = err?.error || 'Erro ao adicionar música.';
        this.isAddingSong = false;
        console.error('[Session] Erro ao adicionar música:', err);
      },
    });
    this.subscriptions.add(addSub);
  }

  removeSong(queueItemId: number) {
    if (!this.sessionCode) return;

    const removeSub = this.karaokeService.removeSong(this.sessionCode, queueItemId).subscribe({
      next: () => {
        console.log('[Session] Música removida com sucesso');
      },
      error: (err) => {
        console.error('[Session] Erro ao remover música', err);
      },
    });
    this.subscriptions.add(removeSub);
  }

  isAddedByMe(item: QueueViewItem): boolean {
    return item.addedByUserName === this.userName;
  }

  get pendingQueue(): QueueViewItem[] {
    const curr = this.current();
    if (!curr) return this.queue();
    return this.queue().filter((s) => s.id !== curr.id);
  }

  // trackBy utilizado no template
  trackById(index: number, item: QueueViewItem): number {
    return item.id;
  }

  // Mapeadores
  private mapEntitiesToView(items: QueueItemEntity[]): QueueViewItem[] {
    return (items || []).map((it) => ({
      id: it.queueItemId,
      songTitle: it.song?.title ?? '',
      youtubeLink: it.song?.url ?? '',
      addedByUserName: it.user?.username ?? '',
    }));
  }

  private mapDtosToView(items: QueueItemDTO[]): QueueViewItem[] {
    return (items || []).map((dto) => this.mapDtoToView(dto));
  }

  private mapDtoToView(dto: QueueItemDTO): QueueViewItem {
    return {
      id: dto.queueItemId,
      songTitle: dto.songTitle,
      youtubeLink: dto.youtubeLink,
      addedByUserName: dto.addedByUserName,
    };
  }
}
