import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, of } from 'rxjs';
import { map, delay } from 'rxjs/operators';

export interface Song {
  id: string;
  titulo: string;
  urlYoutube: string;
  adicionadoPor: string;
  isCurrent: boolean;
}

const initialQueue: Song[] = [
  { id: '1', titulo: 'Bohemian Rhapsody', urlYoutube: 'https://youtube.com/watch?v=1', adicionadoPor: 'Host', isCurrent: true },
  { id: '2', titulo: 'Dancing Queen', urlYoutube: 'https://youtube.com/watch?v=2', adicionadoPor: 'Alice', isCurrent: false },
  { id: '3', titulo: 'Sweet Caroline', urlYoutube: 'https://youtube.com/watch?v=3', adicionadoPor: 'Você', isCurrent: false },
  { id: '4', titulo: "Livin' on a Prayer", urlYoutube: 'https://youtube.com/watch?v=4', adicionadoPor: 'Bob', isCurrent: false },
];

@Injectable({ providedIn: 'root' })
export class KaraokeService {
  // BehaviorSubject para simular atualizações em tempo real
  private queue$ = new BehaviorSubject<Song[]>(initialQueue.slice());

  // Simula validação de sessão (apenas KARAOKE123 é válido)
  validateSession(sessionId: string): Observable<boolean> {
    const valid = sessionId === 'KARAOKE123';
    // small delay to simulate network
    return of(valid).pipe(delay(200));
  }

  getQueue(): Observable<Song[]> {
    return this.queue$.asObservable();
  }

  getCurrentSong(): Observable<Song | null> {
    return this.getQueue().pipe(
      map(list => list.find(s => s.isCurrent) || null)
    );
  }

  // Adiciona uma música validando se é do YouTube (simulado)
  addSong(url: string, addedBy: string): Observable<Song> {
    const isYoutube = /youtube\.com|youtu\.be/.test(url);
    if (!isYoutube) {
      // simula erro com Observable que emite após delay
      return new Observable<Song>(subscriber => {
        setTimeout(() => {
          subscriber.error(new Error('URL inválida ou não suportada.'));
        }, 150);
      });
    }

    // Simula extração de título a partir da URL
    const fakeTitle = this.extractTitleFromUrl(url);
    const newSong: Song = {
      id: this.generateId(),
      titulo: fakeTitle,
      urlYoutube: url,
      adicionadoPor: addedBy,
      isCurrent: false
    };

    const current = this.queue$.getValue().slice();
    current.push(newSong);
    this.queue$.next(current);

    return of(newSong).pipe(delay(100));
  }

  removeSong(songId: string): Observable<boolean> {
    const current = this.queue$.getValue().slice();
    const idx = current.findIndex(s => s.id === songId);
    if (idx === -1) return of(false).pipe(delay(50));

    current.splice(idx, 1);
    this.queue$.next(current);
    return of(true).pipe(delay(80));
  }

  private extractTitleFromUrl(url: string): string {
    // heurística simples para criar um título legível
    try {
      const u = new URL(url);
      const v = u.searchParams.get('v');
      if (v) return `YouTube Song (${v.substring(0, 6)})`;
      // youtu.be short link
      const path = u.pathname.replace('/', '');
      if (path) return `YouTube Song (${path.substring(0, 6)})`;
    } catch (e) {
      // fallback
    }
    return 'YouTube Song (desconhecido)';
  }

  private generateId(): string {
    return Math.random().toString(36).substring(2, 9);
  }
}

