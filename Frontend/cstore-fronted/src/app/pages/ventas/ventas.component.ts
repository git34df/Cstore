import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink, RouterLinkActive } from '@angular/router';
import Swal from 'sweetalert2';
import { VentaService } from '../../core/services/venta.service';
import { TokenService } from '../../core/services/token.service';
import { AuthService } from '../../core/services/auth.service';
import { Router } from '@angular/router';
import { jwtDecode } from 'jwt-decode';
import { SlicePipe, UpperCasePipe } from '@angular/common';

@Component({
  selector: 'app-ventas',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, RouterLinkActive, SlicePipe, UpperCasePipe],
  templateUrl: './ventas.component.html',
  styleUrls: ['./ventas.component.scss'],
})
export class VentasComponent implements OnInit {
  ventas: any[] = [];
  ventasFiltradas: any[] = [];
  cargando = true;
  nombreUsuario = '';
  inicialesUsuario = '';

  // Filtros
  filtroTexto = '';
  filtroEstado = '';

  constructor(
    private ventaService: VentaService,
    private tokenService: TokenService,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    const token = this.tokenService.getToken();
    if (token) {
      try {
        const decoded: any = jwtDecode(token);
        const raw = decoded?.nombre ?? decoded?.name ?? decoded?.sub ?? '';
        this.nombreUsuario = raw.includes('@') ? raw.split('@')[0] : raw;
        this.inicialesUsuario = this.nombreUsuario.slice(0, 2).toUpperCase();
      } catch {}
    }
    this.cargarVentas();
  }

  cargarVentas(): void {
    this.cargando = true;
    this.ventaService.getVentas().subscribe({
      next: (res) => {
        this.ventas = res;
        this.aplicarFiltros();
        this.cargando = false;
      },
      error: () => {
        Swal.fire('Error', 'No se pudo cargar el historial de ventas', 'error');
        this.cargando = false;
      },
    });
  }

  aplicarFiltros(): void {
    let resultado = [...this.ventas];

    if (this.filtroTexto.trim()) {
      const q = this.filtroTexto.toLowerCase().trim();
      resultado = resultado.filter(
        (v) =>
          v.nombreCliente?.toLowerCase().includes(q) ||
          v.emailCliente?.toLowerCase().includes(q) ||
          v.usuarioEmail?.toLowerCase().includes(q) ||
          String(v.id).includes(q)
      );
    }

    if (this.filtroEstado) {
      resultado = resultado.filter((v) => v.estado === this.filtroEstado);
    }

    this.ventasFiltradas = resultado;
  }

  anularVenta(id: number): void {
    Swal.fire({
      title: '¿Anular esta venta?',
      text: 'Se revertirán los pagos asociados. Esta acción no se puede deshacer.',
      icon: 'warning',
      showCancelButton: true,
      confirmButtonColor: '#ef4444',
      cancelButtonText: 'Cancelar',
      confirmButtonText: 'Sí, anular',
    }).then((result) => {
      if (result.isConfirmed) {
        this.ventaService.anularVenta(id).subscribe({
          next: () => {
            Swal.fire('Anulada', 'La venta fue anulada correctamente', 'success');
            this.cargarVentas();
          },
          error: (err) => {
            Swal.fire('Error', err?.error || 'No se pudo anular la venta', 'error');
          },
        });
      }
    });
  }

  descargarPdf(uuid: string): void {
    if (!uuid) {
      Swal.fire('Sin comprobante', 'Esta venta no tiene comprobante PDF', 'info');
      return;
    }
    this.ventaService.getPdf(uuid).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(new Blob([blob], { type: 'application/pdf' }));
        const link = document.createElement('a');
        link.href = url;
        link.download = `Comprobante-${uuid}.pdf`;
        link.click();
        window.URL.revokeObjectURL(url);
      },
      error: () => Swal.fire('Error', 'No se pudo descargar el PDF', 'error'),
    });
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }

  // ── Totales rápidos ──────────────────────────────────────────
  get totalCompletadas(): number {
    return this.ventas.filter((v) => v.estado === 'COMPLETADA').length;
  }
  get totalAnuladas(): number {
    return this.ventas.filter((v) => v.estado === 'ANULADA').length;
  }
  get montoTotal(): number {
    return this.ventas
      .filter((v) => v.estado === 'COMPLETADA')
      .reduce((acc, v) => acc + (v.total ?? 0), 0);
  }
}