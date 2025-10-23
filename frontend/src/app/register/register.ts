import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormGroup, FormControl, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { User } from '../user';
import { AuthService } from '../services/auth.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink], // Adicione ReactiveFormsModule e RouterLink
  templateUrl: './register.html',
  styleUrl: './register.css'
})
export class Register { // <--- Nome da classe é 'Register'

  // Injeta o Router e o UserService
  constructor(
    private router: Router,
    private userService: User, 
    private authService: AuthService
  ) {}

  registerForm = new FormGroup({
    name: new FormControl('', Validators.required), // <--- Campo Nome
    email: new FormControl('', [Validators.required, Validators.email]),
    password: new FormControl('', Validators.required)
  });

  onSubmit() {
    if (this.registerForm.valid) {
      // Salva o nome no serviço
      const name = this.registerForm.value.name || 'Usuário';
      const email = this.registerForm.value.email || '';
      const password = this.registerForm.value.password || '';

      // 1) Chama backend para salvar
      this.authService.register(name, email, password).subscribe({
        next: () => {
          // opcional: auto-login após cadastro
          this.authService.login(email, password).subscribe({
            next: () => this.router.navigate(['/profile']),
            error: () => this.router.navigate(['/login'])
          });
        },
        error: err => {
          console.error('Erro register', err);
          // mostrar mensagem ao usuário
        }
      });
    }
  }
}