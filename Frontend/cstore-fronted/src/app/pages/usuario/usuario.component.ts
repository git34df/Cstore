import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { UsuarioService } from '../../core/services/usuario.service';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-usuario',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './usuario.component.html',
  styleUrls: ['./usuario.component.scss'],
})
export class UsuarioComponent implements OnInit {
  usuarios: any[] = [];
  loading = false;

  constructor(private usuarioService: UsuarioService) {}

  ngOnInit(): void {
    this.loadUsuarios();
  }

  loadUsuarios() {
    this.loading = true;
    this.usuarioService.getAllUsuarios().subscribe({
      next: (res) => {
        this.usuarios = res;
        this.loading = false;
      },
      error: () => {
        Swal.fire('Error', 'No se pudieron cargar los usuarios', 'error');
        this.loading = false;
      },
    });
  }

  // ── Cambiar estado activo/inactivo ──────────────────────
  updateEstado(usuario: any) {
    const nuevoEstado = usuario.status === 'true' ? 'false' : 'true';
    const accion = nuevoEstado === 'true' ? 'activar' : 'desactivar';

    Swal.fire({
      title: `¿${accion.charAt(0).toUpperCase() + accion.slice(1)} usuario?`,
      text: `${usuario.nombre} quedará ${nuevoEstado === 'true' ? 'activo' : 'inactivo'}.`,
      icon: 'question',
      showCancelButton: true,
      confirmButtonColor: nuevoEstado === 'true' ? '#10b981' : '#ef4444',
      cancelButtonText: 'Cancelar',
      confirmButtonText: 'Sí, confirmar',
    }).then((result) => {
      if (!result.isConfirmed) return;

      this.usuarioService.updateUsuario({ id: usuario.id, estado: nuevoEstado }).subscribe({
        next: () => {
          usuario.status = nuevoEstado;
          Swal.fire({
            icon: 'success',
            title: 'Actualizado',
            text: `Usuario ${nuevoEstado === 'true' ? 'activado' : 'desactivado'} correctamente.`,
            timer: 1500,
            showConfirmButton: false,
          });
        },
        error: () => Swal.fire('Error', 'No se pudo actualizar el estado', 'error'),
      });
    });
  }

  // ── Cambiar rol ─────────────────────────────────────────
  updateRol(usuario: any) {
    const rolActual = usuario.rol ?? 'usuario';
    const nuevoRol  = rolActual === 'admin' ? 'usuario' : 'admin';

    Swal.fire({
      title: `¿Cambiar rol a "${nuevoRol}"?`,
      html: `
        <p><strong>${usuario.nombre}</strong> pasará de 
        <span class="swal-badge ${rolActual}">${rolActual}</span> a 
        <span class="swal-badge ${nuevoRol}">${nuevoRol}</span></p>
        <p style="color:#6b7280;font-size:.9rem;margin-top:.5rem">
          ${nuevoRol === 'admin'
            ? '⚠️ Tendrá acceso completo al sistema.'
            : 'Solo tendrá acceso de usuario estándar.'}
        </p>
      `,
      icon: 'warning',
      showCancelButton: true,
      confirmButtonColor: nuevoRol === 'admin' ? '#2962be' : '#6b7280',
      cancelButtonText: 'Cancelar',
      confirmButtonText: 'Sí, cambiar',
    }).then((result) => {
      if (!result.isConfirmed) return;

      this.usuarioService.updateRol({ id: String(usuario.id), rol: nuevoRol }).subscribe({
        next: () => {
          usuario.rol = nuevoRol;
          Swal.fire({
            icon: 'success',
            title: 'Rol actualizado',
            text: `${usuario.nombre} ahora es ${nuevoRol}.`,
            timer: 1500,
            showConfirmButton: false,
          });
        },
        error: () => Swal.fire('Error', 'No se pudo actualizar el rol', 'error'),
      });
    });
  }

  // ── Helper ──────────────────────────────────────────────
  trackById(_: number, item: any) {
    return item.id;
  }
}