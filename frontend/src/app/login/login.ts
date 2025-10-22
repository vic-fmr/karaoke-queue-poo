import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormGroup, FormControl, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink], // Adicione ReactiveFormsModule e RouterLink
  templateUrl: './login.html',
  styleUrl: './login.css'
})
export class Login { // <--- Nome da classe é 'Login'

  // Injeta o Router para poder navegar
  constructor(private router: Router) {}
  
  loginForm = new FormGroup({
    email: new FormControl('', [Validators.required, Validators.email]),
    password: new FormControl('', Validators.required)
  });

  onSubmit() {
    if (this.loginForm.valid) {
      console.log('Login feito:', this.loginForm.value);
      // Redireciona para /profile após o login
      this.router.navigate(['/profile']); 
    }
  }
}