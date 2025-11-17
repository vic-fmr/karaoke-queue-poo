import {Component, OnDestroy} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';
import {ActivatedRoute} from '@angular/router';
import {Subscription} from 'rxjs';
import {KaraokeService, Song} from '../../services/KaraokeService';

@Component({
  selector: 'app-session',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './session.html',
  styleUrls: ['./session.css']
})
export class SessionComponent implements OnDestroy {
  sessionId: string | null = null;
  userName = 'Você';
  connectedUsers = [{
    id: 1,
    name: 'Host'
  }, {
    id: 2,
    name: 'Alice'
  }, {
    id: 3,
    name: 'Bob'
  }]

  queue: Song[] = [];
  current: Song | null = null;

  urlToAdd = '';
  addError: string | null = null;

  private sub = new Subscription();

  constructor(private route: ActivatedRoute, private ks: KaraokeService) {
    this.sessionId = this.route.snapshot.paramMap.get('id');

    this.sub.add(this.ks.getQueue().subscribe(list => {
      this.queue = list;
    }));

    this.sub.add(this.ks.getCurrentSong().subscribe(c => this.current = c));
  }

  ngOnDestroy(): void {
    this.sub.unsubscribe();
  }

  addSong() {
    this.addError = null;
    const url = (this.urlToAdd || '').trim();
    if (!url) {
      this.addError = 'Informe uma URL.';
      return;
    }

    this.ks.addSong(url, this.userName).subscribe({
      next: () => {
        this.urlToAdd = '';
      },
      error: err => {
        this.addError = err?.message || 'Erro ao adicionar.';
      }
    });
  }

  removeSong(songId: string) {
    this.ks.removeSong(songId).subscribe(ok => {
      // opcional: mostrar feedback
    });
  }

  isAddedByMe(song: Song) {
    return song.adicionadoPor === this.userName;
  }

  // items except current
  get pendingQueue() {
    return this.queue.filter(s => !s.isCurrent);
  }

}

