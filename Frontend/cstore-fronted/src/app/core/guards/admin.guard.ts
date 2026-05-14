import { Injectable } from '@angular/core';
import {
  CanActivate,
  Router,
  UrlTree
} from '@angular/router';
import { AuthService } from '../services/auth.service';

@Injectable({
  providedIn: 'root'
})
export class AdminGuard implements CanActivate {

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  canActivate(): boolean | UrlTree {
    const userRole = this.authService.getUserRole();

    if (this.authService.isLoggedIn() && userRole === 'admin') {
      return true;
    } else {
      // Redirige si no es admin o no tiene sesión
      return this.router.createUrlTree(['/unauthorized']);
    }
  }
}
