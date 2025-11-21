import {Component, OnDestroy, OnInit, signal} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';
import {ActivatedRoute, Router, RouterLink} from '@angular/router';
import {Subscription} from 'rxjs';

// Certifique-se de que essas interfaces foram atualizadas no KaraokeService conforme o passo anterior
import {
  KaraokeService, 
  QueueItemEntity, 
  KaraokeSession, 
  AddSongRequest, 
  YouTubeVideo // <--- Novo Import (Interface do vídeo da busca)
} from '../../services/KaraokeService';

import {WebSocketService, FilaUpdate, QueueItemDTO} from '../../services/WebSocketService';
import {AuthService} from '../../services/AuthService';

interface ConnectedUser {
  id: number;
  name: string;
}

// Modelo simplificado para a View
interface QueueViewItem {
  id: number;
  songTitle: string;
  youtubeLink: string;
  addedByUserName: string;
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
  
  // --- NOVOS SIGNALS PARA A BUSCA ---
  searchResults = signal<YouTubeVideo[]>([]); // Armazena a lista de vídeos encontrados
  isSearching = signal<boolean>(false);       // Controla o loading da busca
  isAddingSong: boolean = false;              // Controla o loading da adição (ao clicar no vídeo)

  queue = signal<QueueViewItem[]>([]);
  current = signal<QueueViewItem | null>(null);

  urlToAdd = ''; // Agora funciona como "Termo de busca"
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

    if (!this.sessionCode) {
      this.addError = 'Código da sessão não encontrado!';
      return;
    }

    localStorage.setItem('currentSessionCode', this.sessionCode);
    
    // REMOVIDA A CHAMADA REST INICIAL
    // const initialLoadSub = this.karaokeService.getFairQueue(this.sessionCode).subscribe(...)
    // this.subscriptions.add(initialLoadSub);

    // Conecta ao WebSocket
    this.webSocketService.connect(this.sessionCode);

    // A única fonte da verdade agora é o WebSocket
    const wsSub = this.webSocketService.filaUpdates$.subscribe((filaUpdate: FilaUpdate) => {
      console.log('[WebSocket] Atualização recebida:', filaUpdate);
      
      // Atualiza a fila de músicas
      const mappedQueue = this.mapDtosToView(filaUpdate.songQueue);
      this.queue.set(mappedQueue);
      
      // Atualiza a música atual
      this.current.set(filaUpdate.nowPlaying ? this.mapDtoToView(filaUpdate.nowPlaying) : null);
      
      // ATUALIZA A LISTA DE USUÁRIOS CONECTADOS
      this.connectedUsers.set(filaUpdate.connectedUsers.map(u => ({ id: u.id, name: u.username })));
    });

    this.subscriptions.add(wsSub);
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
    this.webSocketService.disconnect();
  }

  // --- 1. MÉTODO DE BUSCA (O antigo addSong virou busca) ---
  search() {
    const query = (this.urlToAdd || '').trim();
    if (!query) return;

    this.isSearching.set(true);
    this.addError = null;
    this.searchResults.set([]); // Limpa resultados anteriores

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

  // --- 2. MÉTODO DE SELEÇÃO (Novo método que adiciona) ---
  addSelectedSong(video: YouTubeVideo) {
    if (!this.sessionCode) return;

    this.isAddingSong = true;
    this.addError = null;

    // Monta o objeto conforme o novo AddSongRequestDTO do Java
    const request: AddSongRequest = {
      videoId: video.videoId,
      title: video.title,
      thumbnailUrl: video.thumbnail // Atenção: mapeie o campo correto aqui
    };

    const addSub = this.karaokeService.addSong(this.sessionCode, request).subscribe({
      next: () => {
        this.urlToAdd = '';
        this.searchResults.set([]); // Limpa a lista de busca após adicionar
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
      error: (err) => console.error('[Session] Erro ao remover música', err),
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

  trackById(index: number, item: QueueViewItem): number {
    return item.id;
  }

  // --- Mapeadores ---
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