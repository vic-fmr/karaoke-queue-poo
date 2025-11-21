import {Component, OnDestroy, OnInit, signal} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';
import {ActivatedRoute, Router, RouterLink} from '@angular/router';
import {Subscription} from 'rxjs';

// IMPORTANTE: Importe o Pipe que criamos acima
import { SafeUrlPipe } from '../../pipes/safe-url.pipe'; 

import {
  KaraokeService, 
  QueueItemEntity, 
  KaraokeSession, 
  AddSongRequest, 
  YouTubeVideo 
} from '../../services/KaraokeService';

import {WebSocketService, FilaUpdate, QueueItemDTO} from '../../services/WebSocketService';
import {AuthService} from '../../services/AuthService';

interface ConnectedUser {
  id: number;
  name: string;
}

interface QueueViewItem {
  id: number;
  songTitle: string;
  youtubeLink: string; // Aqui esperamos o VIDEO ID
  addedByUserName: string;
}

@Component({
  selector: 'app-host',
  standalone: true,
  // Adicione SafeUrlPipe nos imports
  imports: [CommonModule, FormsModule, RouterLink, SafeUrlPipe], 
  templateUrl: './host.html',
  styleUrls: ['./host.css'],
})
export class Host implements OnInit, OnDestroy {
  sessionCode: string | null = null;
  userName: string = '';
  userId: number = 0;
  
  // Define se este usuário é o Host (quem controla o player)
  // Futuramente, você pode validar isso baseado no ID do criador da sessão
  isHost: boolean = true; 

  connectedUsers = signal<ConnectedUser[]>([]);
  searchResults = signal<YouTubeVideo[]>([]);
  isSearching = signal<boolean>(false);
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
  ) {}

  ngOnInit(): void {
    const currentUser = this.authService.getCurrentUser();
    if (!currentUser) {
      this.router.navigate(['/login']);
      return;
    }

    this.userId = currentUser.id;
    this.userName = currentUser.name;
    this.sessionCode = this.route.snapshot.paramMap.get('id') || localStorage.getItem('currentSessionCode');

    if (!this.sessionCode) {
      this.addError = 'Código da sessão não encontrado!';
      return;
    }

    localStorage.setItem('currentSessionCode', this.sessionCode);

    const joinSub = this.karaokeService.joinSession(this.sessionCode!).subscribe({
      next: () => {
        this.webSocketService.connect(this.sessionCode!);
        const wsSub = this.webSocketService.filaUpdates$.subscribe(filaUpdate => {
          console.log('[WebSocket] Atualização recebida (Host):', filaUpdate);

          // Atualiza a fila de músicas
          const mappedQueue = this.mapDtosToView(filaUpdate.songQueue);
          this.queue.set(mappedQueue);

          // Atualiza a música atual
          this.current.set(filaUpdate.nowPlaying ? this.mapDtoToView(filaUpdate.nowPlaying) : null);

          // ATUALIZA A LISTA DE USUÁRIOS CONECTADOS
          this.connectedUsers.set(filaUpdate.connectedUsers.map(u => ({ id: u.id, name: u.username })));
        });
        this.subscriptions.add(wsSub);
      },
      error: () => { this.addError = 'Erro ao entrar.'; }
    });
    this.subscriptions.add(joinSub);
  }

  ngOnDestroy(): void {
    if (this.sessionCode) {
      this.karaokeService.leaveSession(this.sessionCode!).subscribe({ error: () => {} });
    }
    this.subscriptions.unsubscribe();
    this.webSocketService.disconnect();
  }

  // --- CONTROLES DO HOST (PLAYER) ---
  
  playNextSong() {
    if (!this.sessionCode || !this.isHost) return;
    
    // Chama o endpoint do backend para pular/tocar a próxima
    // O backend remove da fila e avisa via WebSocket, atualizando a tela automaticamente
    this.karaokeService.playNextSong(this.sessionCode).subscribe({
      error: (err) => console.error('Erro ao tocar próxima:', err)
    });
  }

  // --- BUSCA E ADIÇÃO ---

  search() {
    const query = (this.urlToAdd || '').trim();
    if (!query) return;

    this.isSearching.set(true);
    this.addError = null;
    this.searchResults.set([]);

    const searchSub = this.karaokeService.searchVideos(query).subscribe({
      next: (videos) => {
        this.searchResults.set(videos);
        this.isSearching.set(false);
      },
      error: (err) => {
        console.error('Erro na busca:', err);
        this.addError = 'Erro ao buscar vídeos.';
        this.isSearching.set(false);
      }
    });
    this.subscriptions.add(searchSub);
  }

  addSelectedSong(video: YouTubeVideo) {
    if (!this.sessionCode) return;

    this.isAddingSong = true;
    this.addError = null;

    const request: AddSongRequest = {
      videoId: video.videoId,
      title: video.title,
      thumbnailUrl: video.thumbnail
    };

    const addSub = this.karaokeService.addSong(this.sessionCode, request).subscribe({
      next: () => {
        this.urlToAdd = '';
        this.searchResults.set([]);
        this.isAddingSong = false;
      },
      error: (err) => {
        this.addError = err?.error || 'Erro ao adicionar música.';
        this.isAddingSong = false;
      },
    });
    this.subscriptions.add(addSub);
  }

  removeSong(queueItemId: number) {
    if (!this.sessionCode) return;
    this.karaokeService.removeSong(this.sessionCode, queueItemId).subscribe();
  }

  isAddedByMe(item: QueueViewItem): boolean {
    return item.addedByUserName === this.userName;
  }

  get pendingQueue(): QueueViewItem[] {
    const curr = this.current();
    if (!curr) return this.queue();
    return this.queue().filter((s) => s.id !== curr.id);
  }

  trackById(index: number, item: QueueViewItem): number {
    return item.id;
  }

  // --- MAPEADORES ---

  private mapEntitiesToView(items: QueueItemEntity[]): QueueViewItem[] {
    return (items || []).map((it) => ({
      id: it.queueItemId,
      songTitle: it.song?.title ?? '',
      // IMPORTANTE: O player precisa do ID do vídeo aqui. 
      // Se 'it.song.url' for a URL completa, você precisaria extrair o ID. 
      // Assumindo que o backend manda o ID ou URL embeddable.
      youtubeLink: it.song?.youtubeVideoId ?? '', 
      addedByUserName: it.user?.username ?? '',
    }));
  }

  private mapDtosToView(items: QueueItemDTO[]): QueueViewItem[] {
    return (items || []).map((dto) => this.mapDtoToView(dto));
  }

  private mapDtoToView(dto: QueueItemDTO): QueueViewItem {
    console.log('[DEBUG] DTO recebido do Backend:', dto);
    
    return {
      id: dto.queueItemId,
      songTitle: dto.songTitle,
      // Certifique-se que o DTO do backend traz o videoId neste campo
      youtubeLink: dto.youtubeLink, 
      addedByUserName: dto.addedByUserName,
    };
  }
}