import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-welcome-page',
  standalone: true,
  imports: [CommonModule, RouterModule], // Certifique-se de importar RouterModule
  templateUrl: './welcome-page.html',
  styleUrls: ['./welcome-page.css']
})
export class WelcomePage {
  // Nada de lógica de navegação aqui, pois usamos routerLink no HTML
}
