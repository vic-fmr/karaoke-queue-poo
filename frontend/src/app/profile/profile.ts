import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { User } from '../user';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule, RouterLink], // Adicione RouterLink
  templateUrl: './profile.html',
  styleUrl: './profile.css'
})
export class Profile implements OnInit { // <--- Nome 'Profile' e implementa OnInit

  userName: string = '';
  userHandle: string = '';

  // Injeta o serviço
  constructor(private userService: User) {}
  
  // Esta função roda quando o componente carrega
  ngOnInit(): void {
    // Busca os dados do serviço
    this.userName = this.userService.getUserName();
    this.userHandle = this.userService.getUserHandle();
  }
}