import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet } from '@angular/router'; // <--- IMPORTANTE

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterOutlet], // <--- IMPORTANTE
  templateUrl: './app.html',
  styleUrls: ['./app.css']
})
export class App {
  // O formulário foi removido daqui!
  // Esta classe agora está "limpa".
}