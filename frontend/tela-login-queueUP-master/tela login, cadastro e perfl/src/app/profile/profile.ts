import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router'; // 1. Importe o RouterLink

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule, RouterLink], // 2. Adicione o RouterLink
  templateUrl: './profile.html',
  styleUrls: ['./profile.css']
})
export class ProfileComponent {

}
