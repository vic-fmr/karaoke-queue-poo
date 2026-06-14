import {Component, signal} from '@angular/core';
import {CommonModule} from '@angular/common';
import {ReactiveFormsModule, FormGroup, FormControl, Validators} from '@angular/forms';
import {Router, RouterLink} from '@angular/router';
import {AuthService} from '../../services/AuthService';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink], // Adicione ReactiveFormsModule e RouterLink
  templateUrl: './login.html',
  styleUrl: './login.css'
})
export class Login { // <--- Nome da classe é 'Login'

  loginError: string | null = null;
  isLoading = signal(false);

  // Injeta o Router para poder navegar
  constructor(private router: Router, private authService: AuthService) {
  }

  loginForm = new FormGroup({
    email: new FormControl('', [Validators.required, Validators.email]),
    password: new FormControl('', Validators.required)
  });

  onSubmit() {
    if (!this.loginForm.valid) return;

    const email = this.loginForm.value.email || '';
    const password = this.loginForm.value.password || '';

    this.isLoading.set(true);
    this.loginError = null;

    // Usa "email" como username (ajuste se backend usa outro campo)
    this.authService.login(email, password).subscribe({
      next: () => {
        this.isLoading.set(false);
        this.router.navigate(['/home']);
      },
      error: err => {
        console.error('Erro login', err);
        this.isLoading.set(false);
        this.loginError = "Login ou senha inválidos.";
      }
    });
  }
}
