import { Component, OnInit } from '@angular/core';
import { jwtDecode } from 'jwt-decode';
import { TokenService } from '../../core/services/token.service';
import { DashboardService } from '../../core/services/dashboard.service';
import { AuthService } from '../../core/services/auth.service';
import { VentaService } from '../../core/services/venta.service';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { CommonModule, UpperCasePipe, SlicePipe } from '@angular/common';

@Component({
  selector: 'app-admin',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive, UpperCasePipe, SlicePipe],
  templateUrl: './admin.component.html',
  styleUrls: ['./admin.component.scss'],
})
export class AdminComponent implements OnInit {
  user: any;
  nombreUsuario = '';
  inicialesUsuario = '';

  totalCategorias = 0;
  totalProductos = 0;
  totalFacturas = 0;
  totalVentas = 0;
  montoTotal = 0;

  ultimasVentas: any[] = [];
  cargandoVentas = true;

  constructor(
    private tokenService: TokenService,
    private dashboardService: DashboardService,
    private authService: AuthService,
    private ventaService: VentaService,
    private router: Router
  ) {}

  ngOnInit(): void {
    const token = this.tokenService.getToken();
    if (token) {
      try {
        this.user = jwtDecode(token);
        const raw = this.user?.nombre ?? this.user?.name ?? this.user?.sub ?? '';
        this.nombreUsuario = raw.includes('@') ? raw.split('@')[0] : raw;
        this.inicialesUsuario = this.nombreUsuario.slice(0, 2).toUpperCase();
      } catch (err) {
        console.error('Error al decodificar token:', err);
      }
    }

    // Métricas del dashboard
    this.dashboardService.getMetrics().subscribe({
      next: (res) => {
        this.totalCategorias = res.categoria || 0;
        this.totalProductos  = res.producto  || 0;
        this.totalFacturas   = res.Facturas  || 0;
      },
      error: (err) => console.error('Error métricas:', err),
    });

    // Últimas ventas
    this.ventaService.getVentas().subscribe({
      next: (res) => {
        this.totalVentas = res.length;
        this.montoTotal  = res
          .filter((v) => v.estado === 'COMPLETADA')
          .reduce((acc, v) => acc + (v.total ?? 0), 0);
        this.ultimasVentas = res.slice(0, 6);
        this.cargandoVentas = false;
      },
      error: () => {
        this.cargandoVentas = false;
      },
    });
  }

  goToDashboard() {
    this.router.navigate(['/dashboard']);
  }

  goToVentas() {
    this.router.navigate(['/ventas']);
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}