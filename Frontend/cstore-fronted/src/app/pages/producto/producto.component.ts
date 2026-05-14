import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ProductoService } from '../../core/services/producto.service';
import { AuthService } from '../../core/services/auth.service';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-producto',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './producto.component.html',
  styleUrls: ['./producto.component.scss'],
})
export class ProductoComponent implements OnInit {
  productos: any[] = [];
  productoForm: FormGroup;
  editing = false;
  isAdmin = false;

  constructor(
    private fb: FormBuilder,
    private productoService: ProductoService,
    private authService: AuthService
  ) {
    this.productoForm = this.fb.group({
      id_producto: [''],
      nombre_producto: ['', [Validators.required, Validators.minLength(1)]],
      descripcion: ['', [Validators.required, Validators.minLength(4)]],
      precio: ['', [Validators.required, Validators.min(1), Validators.pattern(/^(?!0\d)\d+(\.\d{1,2})?$/)]],
      stock: ['', [Validators.required, Validators.min(0), Validators.pattern(/^\d+$/)]],
      IdCategoria: ['', Validators.required],
    });
  }

  ngOnInit(): void {
    this.isAdmin = this.authService.isAdmin();
    this.loadProductos();
  }

  loadProductos() {
    this.productoService.getAll().subscribe({
      next: (data) => (this.productos = data),
      error: () => Swal.fire('Error', 'No se pudieron cargar los productos', 'error'),
    });
  }

  onSubmit() {
    if (!this.isAdmin || this.productoForm.invalid) return;

    const data = {
      ...this.productoForm.value,
      nombre_producto: this.productoForm.value.nombre_producto.trim(),
      descripcion: this.productoForm.value.descripcion.trim(),
      precio: parseFloat(this.productoForm.value.precio),
      stock: parseInt(this.productoForm.value.stock, 10),
    };

    if (this.editing) {
      this.productoService.updateProducto(data).subscribe({
        next: (res) => {
          Swal.fire('Actualizado', res || 'Producto actualizado correctamente', 'success');
          this.loadProductos();
          this.resetForm();
        },
        error: (err) => {
          if (err.status >= 200 && err.status < 300) {
            Swal.fire('Actualizado', 'Producto actualizado correctamente', 'success');
            this.loadProductos(); this.resetForm();
          } else {
            Swal.fire('Error', err?.error || 'No se pudo actualizar el producto', 'error');
          }
        },
      });
    } else {
      this.productoService.addProducto(data).subscribe({
        next: (res) => {
          Swal.fire('Agregado', res || 'Producto agregado correctamente', 'success');
          this.loadProductos();
          this.resetForm();
        },
        error: (err) => {
          if (err.status >= 200 && err.status < 300) {
            Swal.fire('Agregado', 'Producto agregado correctamente', 'success');
            this.loadProductos(); this.resetForm();
          } else {
            Swal.fire('Error', err?.error || 'No se pudo agregar el producto', 'error');
          }
        },
      });
    }
  }

  editProducto(p: any) {
    if (!this.isAdmin) return;
    this.productoForm.patchValue({
      id_producto: p.id,
      nombre_producto: p.nombre,
      descripcion: p.descripcion,
      precio: p.precio,
      stock: p.stock,
      IdCategoria: p.categoriaId,
    });
    this.editing = true;
  }

  deleteProducto(id: number) {
    if (!this.isAdmin) return;
    Swal.fire({
      title: '¿Eliminar producto?',
      text: 'Esta acción no se puede deshacer.',
      icon: 'warning',
      showCancelButton: true,
      confirmButtonText: 'Sí, eliminar',
      cancelButtonText: 'Cancelar',
      confirmButtonColor: '#ef4444',
    }).then((res) => {
      if (res.isConfirmed) {
        this.productoService.deleteProducto(id).subscribe({
          next: (resp) => {
            Swal.fire('Eliminado', resp || 'Producto eliminado correctamente', 'success');
            this.loadProductos();
          },
          error: (err) => {
            if (err.status >= 200 && err.status < 300) {
              Swal.fire('Eliminado', 'Producto eliminado correctamente', 'success');
              this.loadProductos();
            } else {
              Swal.fire('Error', err?.error || 'No se pudo eliminar el producto', 'error');
            }
          },
        });
      }
    });
  }

  toggleStatus(p: any) {
    if (!this.isAdmin) return;
    const nuevoEstado = p.estado === 'Activo' ? 'Inactivo' : 'Activo';
    this.productoService.updateStatus(p.id, nuevoEstado).subscribe({
      next: (res) => {
        Swal.fire('Actualizado', res, 'success');
        this.loadProductos();
      },
      error: () => Swal.fire('Error', 'No se pudo actualizar el estado', 'error'),
    });
  }

  resetForm() {
    this.productoForm.reset();
    this.editing = false;
  }
}
