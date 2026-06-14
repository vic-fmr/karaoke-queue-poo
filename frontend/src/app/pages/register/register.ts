import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormGroup, FormControl, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../services/AuthService';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink], // Adicione ReactiveFormsModule e RouterLink
  templateUrl: './register.html',
  styleUrl: './register.css',
})
export class Register {
  // <--- Nome da classe é 'Register'

  signUpError: string | null = null;
  isLoading = signal(false);

  // Injeta o Router e o UserService
  constructor(private router: Router, private authService: AuthService) {}

  registerForm = new FormGroup({
    name: new FormControl('', Validators.required), // <--- Campo Nome
    email: new FormControl('', [Validators.required, Validators.email]),
    password: new FormControl('', Validators.required),
  });

  onSubmit() {
    if (this.registerForm.valid) {
      this.isLoading.set(true);
      this.signUpError = null;

      // Salva o nome no serviço
      const username = this.registerForm.value.name || 'Usuário';
      const email = this.registerForm.value.email || '';
      const password = this.registerForm.value.password || '';

      this.authService.register(username, email, password).subscribe({
        next: () => {
          // opcional: auto-login após cadastro
          this.authService.login(email, password).subscribe({
            next: () => {
              this.isLoading.set(false);
              this.router.navigate(['/home']);
            },
            error: () => {
              this.isLoading.set(false);
              this.router.navigate(['/login']);
            },
          });
        },
        error: (err) => {
          console.error('Erro register', err);
          this.isLoading.set(false);
          this.signUpError = err.error?.message || 'Erro ao registrar usuário.';
        },
      });
    }
  }
}
