import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormGroup, FormControl, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { User } from '../user';

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
    private userService: User 
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
      this.userService.registerUser(name);
      
      // Redireciona para /profile após o cadastro
      this.router.navigate(['/profile']);
    }
  }
}