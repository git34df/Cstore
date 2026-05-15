import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink, RouterLinkActive } from '@angular/router';
import Swal from 'sweetalert2';
import { BillService } from '../../core/services/bill.service';
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

  filtroTexto = '';
  filtroEstado = '';

  constructor(
    private billService: BillService,
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
    this.billService.getBills().subscribe({
      next: (res: any[]) => {
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
          v.nombre?.toLowerCase().includes(q) ||
          v.email?.toLowerCase().includes(q) ||
          v.createdby?.toLowerCase().includes(q) ||
          String(v.id).includes(q)
      );
    }

    // Bill no tiene estado, el filtro de estado no aplica
    this.ventasFiltradas = resultado;
  }

  borrarVenta(id: number): void {
    Swal.fire({
      title: '¿Eliminar esta venta?',
      text: 'Esta acción no se puede deshacer.',
      icon: 'warning',
      showCancelButton: true,
      confirmButtonColor: '#ef4444',
      cancelButtonText: 'Cancelar',
      confirmButtonText: 'Sí, eliminar',
    }).then((result) => {
      if (result.isConfirmed) {
        this.billService.deleteBill(id).subscribe({
          next: () => {
            Swal.fire('Eliminada', 'La venta fue eliminada correctamente', 'success');
            this.cargarVentas();
          },
          error: () => Swal.fire('Error', 'No se pudo eliminar la venta', 'error'),
        });
      }
    });
  }

  descargarPdf(uuid: string): void {
    if (!uuid) {
      Swal.fire('Sin comprobante', 'Esta venta no tiene comprobante PDF', 'info');
      return;
    }
    this.billService.getPdf(uuid).subscribe({
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

  get totalCompletadas(): number { return this.ventas.length; }
  get totalAnuladas(): number { return 0; }
  get montoTotal(): number {
    return this.ventas.reduce((acc, v) => acc + (v.totalConIgv ?? 0), 0);
  }
}