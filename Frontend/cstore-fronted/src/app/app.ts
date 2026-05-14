import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterOutlet],
  template: `
    <main class="app-container">
      <router-outlet></router-outlet>
    </main>
  `,
  styles: [`
    .app-container {
      font-family: Arial, sans-serif;
      min-height: 100vh;
      background-color: #f9f9f9;
      padding: 1rem;
    }
  `]
})
export class AppComponent {
  title = 'Cstore Frontend';
}

