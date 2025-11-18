import {Component} from '@angular/core';
import {CommonModule} from '@angular/common';
import {ReactiveFormsModule, FormControl, Validators} from '@angular/forms';
import {Router, RouterLink} from '@angular/router';
import {KaraokeService} from '../../services/KaraokeService';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './home.html',
  styleUrls: ['./home.css'],
})
export class Home {
  sessionId = new FormControl('', [Validators.required]);
  error: string | null = null;
  loading: boolean = false;

  constructor(private router: Router, private ks: KaraokeService) {
  }

  enter() {
    this.error = null;
    this.loading = true;

    const id = (this.sessionId.value || '').toString().trim();
    if (!id) {
      this.error = 'Informe o ID da sessão.';
      this.loading = false;
      return;
    }

    this.ks.getSession(id).subscribe({
      next: (valid) => {
        if (valid) {
          this.router.navigate(['/session', id]);
        } else {
          this.error = 'Sessão não encontrada.';
        }
        this.loading = false;
      },
      error: (err) => {
        console.log("Erro ao buscar sessão:", err);
        this.error = 'Sessão não encontrada.';
        this.loading = false;
      }
    });


  }
}
