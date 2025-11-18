import {Injectable} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {Observable} from 'rxjs';

// Modelos conforme backend (entidades)
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

// DTO usado para adicionar música
export interface AddSongRequest {
  songTitle: string;
}

export interface YouTubeVideo {
  videoId: string;
  title: string;
  thumbnailUrl: string;
}

@Injectable({providedIn: 'root'})
export class KaraokeService {
  private baseUrl = 'http://localhost:8080';
  private sessionsApiUrl = `${this.baseUrl}/api/sessions`;
  private videosApiUrl = `${this.baseUrl}/api/videos`;

  constructor(private http: HttpClient) {
  }

  // --- Métodos de Sessão (REST) ---

  getSession(sessionCode: string): Observable<KaraokeSession> {
    return this.http.get<KaraokeSession>(`${this.sessionsApiUrl}/${sessionCode}`);
  }

  addSong(sessionCode: string, request: AddSongRequest): Observable<void> {
    return this.http.post<void>(`${this.sessionsApiUrl}/${sessionCode}/queue`, request);
  }

  removeSong(sessionCode: string, queueItemId: number): Observable<void> {
    return this.http.delete<void>(`${this.sessionsApiUrl}/${sessionCode}/queue/${queueItemId}`);
  }

  createSession(): Observable<KaraokeSession> {
    return this.http.post<KaraokeSession>(this.sessionsApiUrl, {});
  }

  // --- Métodos de Vídeo (REST) ---

  searchVideos(query: string): Observable<YouTubeVideo[]> {
    const params = new HttpParams().set('query', query);
    return this.http.get<YouTubeVideo[]>(`${this.videosApiUrl}/search`, {params});
  }
}
