import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { ClienteService } from '../../core/services/cliente.service';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-cliente',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './cliente.component.html',
  styleUrls: ['./cliente.component.scss'],
})
export class ClienteComponent implements OnInit {
  clientes: any[] = [];
  loading = false;

  // Modal
  modalVisible = false;
  loadingModal = false;
  clienteSeleccionado: any = null;

  constructor(private clienteService: ClienteService) {}

  ngOnInit(): void {
    this.loadClientes();
  }

  loadClientes(): void {
    this.loading = true;
    this.clienteService.getAllClientes().subscribe({
      next: (res) => {
        this.clientes = res;
        this.loading = false;
      },
      error: () => {
        Swal.fire('Error', 'No se pudieron cargar los clientes', 'error');
        this.loading = false;
      },
    });
  }

  verResumen(cliente: any): void {
    this.modalVisible = true;
    this.loadingModal = true;
    this.clienteSeleccionado = null;

    this.clienteService.getClienteResumen(cliente.id).subscribe({
      next: (res) => {
        this.clienteSeleccionado = res;
        this.loadingModal = false;
      },
      error: () => {
        Swal.fire('Error', 'No se pudo cargar el resumen del cliente', 'error');
        this.cerrarModal();
      },
    });
  }

  cerrarModal(): void {
    this.modalVisible = false;
    this.clienteSeleccionado = null;
    this.loadingModal = false;
  }

  trackById(_: number, item: any) {
    return item.id;
  }
}
