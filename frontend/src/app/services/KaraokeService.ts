import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

// --- Interfaces ---
export interface AddSongRequest {
  songTitle: string;
  userId: string;
  userName: string;
}

export interface Song {
  id: string;
  titulo: string;
  urlYoutube: string;
  adicionadoPor: string;
}

export interface KaraokeSession {
  accessCode: string;
  queue: Song[];
  currentSong: Song | null;
}

export interface YouTubeVideo {
  videoId: string;
  title: string;
  thumbnailUrl: string;
}

@Injectable({ providedIn: 'root' })
export class KaraokeService {
  private baseUrl = 'http://localhost:8080';
  private sessionsApiUrl = `${this.baseUrl}/api/sessions`;
  private videosApiUrl = `${this.baseUrl}/api/videos`;

  constructor(private http: HttpClient) {}

  // --- Métodos de Sessão (REST) ---

  getSession(sessionCode: string): Observable<KaraokeSession> {
    return this.http.get<KaraokeSession>(`${this.sessionsApiUrl}/${sessionCode}`);
  }

  addSong(sessionCode: string, request: AddSongRequest): Observable<void> {
    return this.http.post<void>(`${this.sessionsApiUrl}/${sessionCode}/queue`, request);
  }

  removeSong(sessionCode: string, songId: string): Observable<void> {
    // O endpoint correto do seu backend é /queue/{queueItemId}
    return this.http.delete<void>(`${this.sessionsApiUrl}/${sessionCode}/queue/${songId}`);
  }

  createSession(): Observable<KaraokeSession> {
    return this.http.post<KaraokeSession>(this.sessionsApiUrl, {});
  }

  // --- Métodos de Vídeo (REST) ---

  searchVideos(query: string): Observable<YouTubeVideo[]> {
    const params = new HttpParams().set('query', query);
    return this.http.get<YouTubeVideo[]>(`${this.videosApiUrl}/search`, { params });
  }
}
