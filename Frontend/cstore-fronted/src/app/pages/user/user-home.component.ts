import { Component, OnInit } from '@angular/core';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { CommonModule, UpperCasePipe, SlicePipe } from '@angular/common';
import { jwtDecode } from 'jwt-decode';
import { TokenService } from '../../core/services/token.service';
import { AuthService } from '../../core/services/auth.service';
import { BillService } from '../../core/services/bill.service';

@Component({
  selector: 'app-user-home',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive, UpperCasePipe, SlicePipe],
  templateUrl: './user-home.component.html',
  styleUrls: ['./user-home.component.scss'],
})
export class UserHomeComponent implements OnInit {
  nombreUsuario = '';
  emailUsuario  = '';
  totalMisVentas = 0;
  misVentas: any[] = [];
  cargando = true;

  constructor(
    private tokenService: TokenService,
    private authService: AuthService,
    private billService: BillService,
    private router: Router
  ) {}

  ngOnInit(): void {
    // Decodificar token
    const token = this.tokenService.getToken();
    if (token) {
      try {
        const decoded: any = jwtDecode(token);
        const raw = decoded?.nombre ?? decoded?.name ?? decoded?.sub ?? '';
        this.emailUsuario  = decoded?.sub ?? '';
        this.nombreUsuario = raw.includes('@') ? raw.split('@')[0] : raw;
      } catch {}
    }

    // Cargar facturas y filtrar por email del usuario logueado
    this.billService.getBills().subscribe({
      next: (res: any[]) => {
        const todas = Array.isArray(res) ? res : [];
        const mias  = todas.filter((f: any) =>
          f.email?.toLowerCase() === this.emailUsuario?.toLowerCase()
        );
        this.totalMisVentas = mias.length;
        this.misVentas      = mias.slice(0, 5);
        this.cargando       = false;
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