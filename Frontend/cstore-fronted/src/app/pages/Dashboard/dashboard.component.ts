import { Component, OnInit, AfterViewInit, ViewChild, ElementRef, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DashboardService } from '../../core/services/dashboard.service';
import { TokenService } from '../../core/services/token.service';
import { jwtDecode as jwt_decode } from 'jwt-decode';
import { Chart, registerables } from 'chart.js';

Chart.register(...registerables);

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss']
})
export class DashboardComponent implements OnInit, AfterViewInit, OnDestroy {

  @ViewChild('chartUnidades') chartUnidadesRef!: ElementRef<HTMLCanvasElement>;
  @ViewChild('chartIngresos') chartIngresosRef!: ElementRef<HTMLCanvasElement>;

  user: any = null;
  metrics: any = null;
  loading: boolean = true;
  errorMessage: string | null = null;

  totalIngresos: number = 0;
  stockTotal: number = 0;
  precioPromedio: number = 0;
  topProductosUnidades: any[] = [];
  topProductosIngresos: any[] = [];

  private chartUnidades: Chart | null = null;
  private chartIngresos: Chart | null = null;
  private dataLoaded: boolean = false;
  private viewReady: boolean = false;

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

    this.dashboardService.getMetrics().subscribe({
      next: (res) => {
        this.metrics = res;
        this.totalIngresos = res.totalIngresos || 0;
        this.stockTotal = res.stockTotal || 0;
        this.precioPromedio = res.precioPromedio || 0;
        this.topProductosUnidades = res.topProductosUnidades || [];
        this.topProductosIngresos = res.topProductosIngresos || [];
        this.loading = false;
        this.dataLoaded = true;
        if (this.viewReady) {
          this.buildCharts();
        }
      },
      error: (err) => {
        console.error('Error al obtener métricas', err);
        this.errorMessage = 'No se pudieron cargar las métricas';
        this.loading = false;
      }
    });
  }

  ngAfterViewInit(): void {
    this.viewReady = true;
    if (this.dataLoaded) {
      this.buildCharts();
    }
  }

  ngOnDestroy(): void {
    this.chartUnidades?.destroy();
    this.chartIngresos?.destroy();
  }

  private buildCharts(): void {
    setTimeout(() => {
      this.buildChartUnidades();
      this.buildChartIngresos();
    }, 50);
  }

  private buildChartUnidades(): void {
    if (!this.chartUnidadesRef?.nativeElement) return;
    this.chartUnidades?.destroy();

    const labels = this.topProductosUnidades.map(p => p.nombre);
    const data = this.topProductosUnidades.map(p => p.unidades);

    this.chartUnidades = new Chart(this.chartUnidadesRef.nativeElement, {
      type: 'bar',
      data: {
        labels,
        datasets: [{
          label: 'Unidades vendidas',
          data,
          backgroundColor: '#7F77DD',
          borderRadius: 6,
          borderSkipped: false
        }]
      },
      options: this.getChartOptions('unidades')
    });
  }

  private buildChartIngresos(): void {
    if (!this.chartIngresosRef?.nativeElement) return;
    this.chartIngresos?.destroy();

    const labels = this.topProductosIngresos.map(p => p.nombre);
    const data = this.topProductosIngresos.map(p => p.ingresos);

    this.chartIngresos = new Chart(this.chartIngresosRef.nativeElement, {
      type: 'bar',
      data: {
        labels,
        datasets: [{
          label: 'Ingresos (S/)',
          data,
          backgroundColor: '#1D9E75',
          borderRadius: 6,
          borderSkipped: false
        }]
      },
      options: this.getChartOptions('ingresos')
    });
  }

  private getChartOptions(type: 'unidades' | 'ingresos'): any {
    return {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: { display: false },
        tooltip: {
          backgroundColor: '#ffffff',
          titleColor: '#111827',
          bodyColor: '#6b7280',
          borderColor: '#e5e7eb',
          borderWidth: 1,
          padding: 10,
          cornerRadius: 8,
          callbacks: {
            label: (ctx: any) => {
              const val = ctx.parsed.y;
              return type === 'ingresos'
                ? ` S/ ${val.toLocaleString('es-PE', { minimumFractionDigits: 2 })}`
                : ` ${val} unidades`;
            }
          }
        }
      },
      scales: {
        x: {
          ticks: { color: '#9ca3af', font: { size: 12 } },
          grid: { color: 'rgba(0,0,0,0.05)' },
          border: { display: false }
        },
        y: {
          ticks: {
            color: '#9ca3af',
            font: { size: 12 },
            callback: (val: any) =>
              type === 'ingresos' ? `S/ ${Number(val).toLocaleString('es-PE')}` : val
          },
          grid: { color: 'rgba(0,0,0,0.05)' },
          border: { display: false }
        }
      }
    };
  }
}