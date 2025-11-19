import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

// --- INTERFACES (Models) ---

export interface User {
  id: number;
  username: string;
  email?: string;
}

export interface Song {
  songId: number;
  youtubeVideoId: string;
  title: string;
  artist: string;
  url: string;
}

export interface QueueItemEntity {
  queueItemId: number;
  song: Song;
  user: User;
  timestampAdded: string;
}

export interface KaraokeSession {
  id: number;
  accessCode: string;
  status: 'WAITING' | 'PLAYING' | 'CLOSED' | string;
  connectedUsers: User[];
  songQueue: QueueItemEntity[];
}

// 1. ATUALIZADO: Interface para o POST (corpo da requisição)
// Deve bater com o AddSongRequestDTO do Java
export interface AddSongRequest {
  videoId: string;
  title: string;
  thumbnailUrl: string;
}

// 2. ATUALIZADO: Interface para o GET (resultado da busca)
// Deve bater com o YouTubeVideoDTO (Lombok) do Java
export interface YouTubeVideo {
  videoId: string;
  title: string;
  thumbnail: string; // O Lombok gera 'thumbnail' baseada na variavel
  embedUrl: string;
  probablyEmbeddable: boolean;
}

@Injectable({ providedIn: 'root' })
export class KaraokeService {
  private baseUrl = 'http://localhost:8080';
  
  // Ajuste conforme suas rotas no Backend.
  // Se seu SongController tem @RequestMapping("/songs"), a busca fica em /songs/search
  private sessionsApiUrl = `${this.baseUrl}/api/sessions`;
  private songsApiUrl = `${this.baseUrl}/api/videos`; 

  constructor(private http: HttpClient) {}

  // --- Métodos de Sessão ---

  getSession(sessionCode: string): Observable<KaraokeSession> {
    return this.http.get<KaraokeSession>(`${this.sessionsApiUrl}/${sessionCode}`);
  }

  // O request agora envia o objeto completo do vídeo escolhido
  addSong(sessionCode: string, request: AddSongRequest): Observable<void> {
    // O endpoint aqui deve bater com o @PostMapping("/{sessionCode}/queue")
    // Se esse endpoint estiver no SongController, use songsApiUrl.
    // Se estiver num SessionController, mantenha sessionsApiUrl.
    // Vou assumir que a fila pertence à sessão:
    return this.http.post<void>(`${this.sessionsApiUrl}/${sessionCode}/queue`, request);
  }

  removeSong(sessionCode: string, queueItemId: number): Observable<void> {
    return this.http.delete<void>(`${this.sessionsApiUrl}/${sessionCode}/queue/${queueItemId}`);
  }

  createSession(): Observable<KaraokeSession> {
    return this.http.post<KaraokeSession>(this.sessionsApiUrl, {});
  }

  // --- Métodos de Vídeo / Busca ---

  searchVideos(query: string): Observable<YouTubeVideo[]> {
    const params = new HttpParams().set('query', query);
    // Ajustado para bater com SongController @GetMapping("/search")
    return this.http.get<YouTubeVideo[]>(`${this.songsApiUrl}/search`, { params });
  }

  playNextSong(sessionCode: string): Observable<QueueItemEntity | null> {
    return this.http.post<QueueItemEntity | null>(`${this.sessionsApiUrl}/${sessionCode}/queue/next`, {});
  }
}
