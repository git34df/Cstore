
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DashboardService } from '../../core/services/dashboard.service';
import { TokenService } from '../../core/services/token.service';
import { jwtDecode as jwt_decode } from 'jwt-decode';

@Component({
  selector: 'app-dashboard',
  standalone: true, 
  imports: [CommonModule], 
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss']
})
export class DashboardComponent implements OnInit {
  user: any = null;
  metrics: any = null;
  loading: boolean = true;
  errorMessage: string | null = null;

  // KPIs desde el backend
  totalIngresos: number = 0;
  stockTotal: number = 0;
  precioPromedio: number = 0;
  topProductosUnidades: any[] = [];
  topProductosIngresos: any[] = [];

  constructor(
    private dashboardService: DashboardService,
    private tokenService: TokenService
  ) {}

  ngOnInit(): void {
    const token = this.tokenService.getToken();
    if (token) {
      try {
        this.user = jwt_decode(token);
      } catch (err) {
        console.error('Error al decodificar token', err);
        this.errorMessage = 'Token inválido';
        this.loading = false;
      }
    }

    // Cargar todas las métricas desde el backend
    this.dashboardService.getMetrics().subscribe({
      next: (res) => {
        this.metrics = res;
        
        // DEBUG: Ver qué está llegando
        console.log('Métricas recibidas:', res);
        console.log('Top Productos Unidades:', res.topProductosUnidades);
        console.log('Top Productos Ingresos:', res.topProductosIngresos);
        
        // Asignar los nuevos KPIs
        this.totalIngresos = res.totalIngresos || 0;
        this.stockTotal = res.stockTotal || 0;
        this.precioPromedio = res.precioPromedio || 0;
        this.topProductosUnidades = res.topProductosUnidades || [];
        this.topProductosIngresos = res.topProductosIngresos || [];
        
        this.loading = false;
      },
      error: (err) => {
        console.error('Error al obtener métricas', err);
        this.errorMessage = 'No se pudieron cargar las métricas';
        this.loading = false;
      }
    });
  }

  getMaxValue(items: any[], key: string): number {
    if (!items || items.length === 0) return 0;
    return Math.max(...items.map(item => item[key]));
  }

  getBarWidth(value: number, maxValue: number): string {
    if (!maxValue) return '0%';
    return `${(value / maxValue) * 100}%`;
  }
}
