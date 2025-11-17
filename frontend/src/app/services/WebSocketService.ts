import { Injectable } from '@angular/core';
import { Client, IMessage } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { Observable, Subject } from 'rxjs';
import { KaraokeSession } from './KaraokeService';

@Injectable({
  providedIn: 'root',
})
export class WebSocketService {
  private client: Client;
  private sessionUpdatesSubject = new Subject<KaraokeSession>();
  public sessionUpdates$: Observable<KaraokeSession>;

  constructor() {
    this.sessionUpdates$ = this.sessionUpdatesSubject.asObservable();
    this.client = new Client({
      webSocketFactory: () => new SockJS('http://localhost:8080/ws'), // URL completa do backend
      reconnectDelay: 5000,
      debug: (str) => {
        console.log('STOMP: ' + str);
      },
    });
  }

  connect(sessionCode: string): void {
    if (this.client.active) {
      console.log('STOMP client já está ativo.');
      return;
    }

    this.client.onConnect = (frame) => {
      console.log('Conectado ao WebSocket: ' + frame);
      // Se inscreve no tópico específico da sessão para receber atualizações
      this.client.subscribe(`/topic/session/${sessionCode}`, (message: IMessage) => {
        const sessionState: KaraokeSession = JSON.parse(message.body);
        this.sessionUpdatesSubject.next(sessionState);
      });
    };

    this.client.onStompError = (frame) => {
      console.error('Erro no broker: ' + frame.headers['message']);
      console.error('Detalhes: ' + frame.body);
    };

    this.client.activate();
  }

  disconnect(): void {
    if (this.client.active) {
      this.client.deactivate();
    }
  }
}
