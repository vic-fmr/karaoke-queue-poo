import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormGroup, FormControl, Validators } from '@angular/forms'; 
import { RouterLink, Router } from '@angular/router'; // 1. Importe o Router

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './login.html',
  styleUrls: ['./login.css']
})
export class LoginComponent {

  constructor(private router: Router) {}
  
  loginForm = new FormGroup({
    email: new FormControl('', [Validators.required, Validators.email]),
    password: new FormControl('', Validators.required)
  });

  onSubmit() {
    if (this.loginForm.valid) {
      console.log('Formulário de login enviado:', this.loginForm.value);
      
      this.router.navigate(['/profile']);

    } else {
      console.log('Formulário inválido');
    }
  }
}