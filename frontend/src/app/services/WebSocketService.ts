import {Injectable} from '@angular/core';
import {Client, IMessage} from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import {Observable, Subject} from 'rxjs';
import {environment} from '../enviroments/environment';

// Interface que corresponde ao FilaUpdateDTO do backend
export interface QueueItemDTO {
  songTitle: string;
  youtubeLink: string;
  addedByUserName: string;
  queueItemId: number;
}

// Interface para representar um usuário conectado
export interface ConnectedUser {
  id: number;
  username: string;
}

export interface FilaUpdate {
  songQueue: QueueItemDTO[];
  nowPlaying: QueueItemDTO | null;
  sessionStatus: string;
  connectedUsers: ConnectedUser[];
}

@Injectable({
  providedIn: 'root',
})
export class WebSocketService {
  private client: Client;
  private filaUpdatesSubject = new Subject<FilaUpdate>();
  public filaUpdates$: Observable<FilaUpdate>;

  constructor() {
    this.filaUpdates$ = this.filaUpdatesSubject.asObservable();
    this.client = new Client({
      webSocketFactory: () => {
        console.log(`[WebSocket] Criando conexão SockJS para ${environment.apiUrl}/ws`);
        return new SockJS(`${environment.apiUrl}/ws`);
      },
      reconnectDelay: 5000,
      debug: (str) => {
        console.log('[STOMP Debug]', str);
      },
    });
  }

  connect(sessionCode: string): void {
    if (this.client.active) {
      console.log('[WebSocket] Cliente STOMP já está ativo.');
      return;
    }

    console.log('[WebSocket] Iniciando conexão...');

    this.client.onConnect = (frame) => {
      console.log('[WebSocket] ✓ Conectado com sucesso!', frame);

      const topic = `/topic/fila/${sessionCode}`;
      console.log(`[WebSocket] Inscrevendo-se no tópico: ${topic}`);

      this.client.subscribe(topic, (message: IMessage) => {
        console.log('[WebSocket] ✓ Mensagem recebida:', message.body);
        try {
          const filaUpdate = JSON.parse(message.body) as FilaUpdate;
          console.log('[WebSocket] Estado da fila:', filaUpdate);
          this.filaUpdatesSubject.next(filaUpdate);
        } catch (e) {
          console.error('[WebSocket] Erro ao processar mensagem:', e);
        }
      });

      console.log('[WebSocket] Inscrição concluída com sucesso');
    };

    this.client.onStompError = (frame) => {
      console.error('[WebSocket] ✗ Erro STOMP:', frame.headers['message']);
      console.error('[WebSocket] Detalhes:', frame.body);
    };

    this.client.onWebSocketClose = (evt) => {
      console.warn('[WebSocket] Conexão fechada:', evt);
    };

    this.client.activate();
  }

  disconnect(): void {
    if (this.client.active) {
      console.log('[WebSocket] Desconectando...');
      this.client.deactivate();
    }
  }
}
