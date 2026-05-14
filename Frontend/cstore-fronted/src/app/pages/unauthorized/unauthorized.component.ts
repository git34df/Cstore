import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-unauthorized',
  standalone: true,
  templateUrl: './unauthorized.component.html',
  styleUrls: ['./unauthorized.component.scss'],
})
export class UnauthorizedComponent {
  constructor(private router: Router, private authService: AuthService) {}

  goBack(): void {
    const rol = this.authService.getUserRole();
    if (rol === 'admin') {
      this.router.navigate(['/admin']);
    } else if (rol) {
      this.router.navigate(['/user-home']);
    } else {
      this.router.navigate(['/login']);
    }
  }
}