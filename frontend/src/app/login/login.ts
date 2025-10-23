import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormGroup, FormControl, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink], // Adicione ReactiveFormsModule e RouterLink
  templateUrl: './login.html',
  styleUrl: './login.css'
})
export class Login { // <--- Nome da classe é 'Login'

  // Injeta o Router para poder navegar
  constructor(private router: Router, private authService: AuthService) {}
  
  loginForm = new FormGroup({
    email: new FormControl('', [Validators.required, Validators.email]),
    password: new FormControl('', Validators.required)
  });

  onSubmit() {
    if (!this.loginForm.valid) return;

    const email = this.loginForm.value.email || '';
    const password = this.loginForm.value.password || '';

    // Usa "email" como username (ajuste se backend usa outro campo)
    this.authService.login(email, password).subscribe({
      next: () => this.router.navigate(['/profile']),
      error: err => {
        console.error('Erro login', err);
        // mostrar mensagem ao usuário (implemente UI)
      }
    });
  }
}