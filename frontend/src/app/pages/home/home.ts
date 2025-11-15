import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormControl, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { KaraokeService } from '../../services/KaraokeService';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './home.html',
  styleUrls: ['./home.css']
})
export class Home {
  sessionId = new FormControl('', [Validators.required]);
  error: string | null = null;

  constructor(private router: Router, private ks: KaraokeService) {}

  enter() {
    this.error = null;
    const id = (this.sessionId.value || '').toString().trim();
    if (!id) {
      this.error = 'Informe o ID da sessão.';
      return;
    }

    this.ks.validateSession(id).subscribe(valid => {
      if (valid) {
        this.router.navigate(['/session', id]);
      } else {
        this.error = 'Sessão não encontrada.';
      }
    });
  }
}

