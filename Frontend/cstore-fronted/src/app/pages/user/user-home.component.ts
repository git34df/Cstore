import { Component, OnInit } from '@angular/core';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { CommonModule, UpperCasePipe, SlicePipe } from '@angular/common';
import { jwtDecode } from 'jwt-decode';
import { TokenService } from '../../core/services/token.service';
import { AuthService } from '../../core/services/auth.service';
import { VentaService } from '../../core/services/venta.service';

@Component({
  selector: 'app-user-home',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive, UpperCasePipe, SlicePipe],
  templateUrl: './user-home.component.html',
  styleUrls: ['./user-home.component.scss'],
})
export class UserHomeComponent implements OnInit {
  nombreUsuario = '';
  totalMisVentas = 0;
  misVentas: any[] = [];
  cargando = true;

  constructor(
    private tokenService: TokenService,
    private authService: AuthService,
    private ventaService: VentaService,
    private router: Router
  ) {}

  ngOnInit(): void {
    const token = this.tokenService.getToken();
    if (token) {
      try {
        const decoded: any = jwtDecode(token);
        const raw = decoded?.nombre ?? decoded?.name ?? decoded?.sub ?? '';
        this.nombreUsuario = raw.includes('@') ? raw.split('@')[0] : raw;
      } catch {}
    }

    this.ventaService.getVentas().subscribe({
      next: (res) => {
        this.misVentas = res.slice(0, 5); // últimas 5 en el home
        this.totalMisVentas = res.length;
        this.cargando = false;
      },
      error: () => {
        this.cargando = false;
      },
    });
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }

  irAOrdenes(): void {
    this.router.navigate(['/ordenes']);
  }
}