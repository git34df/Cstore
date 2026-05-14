import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { CategoriaService } from '../../core/services/categoria.service';
import { AuthService } from '../../core/services/auth.service';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-categoria',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './categoria.component.html',
  styleUrls: ['./categoria.component.scss'],
})
export class CategoriaComponent implements OnInit {
  categorias: any[] = [];
  categoriaForm!: FormGroup;
  editing = false;
  isAdmin = false;

  constructor(
    private categoriaService: CategoriaService,
    private fb: FormBuilder,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.isAdmin = this.authService.isAdmin();
    this.initForm();
    this.loadCategorias();
  }

  private initForm(): void {
    this.categoriaForm = this.fb.group({
      IdCategoria: [null],
      nombre: ['', [Validators.required, Validators.minLength(3)]],
      descripcion: [''],
    });
  }

  loadCategorias(): void {
    this.categoriaService.getAll().subscribe({
      next: (data) => {
        this.categorias = data;
      },
      error: () => {
        Swal.fire('Error', 'No se pudieron cargar las categorías', 'error');
      },
    });
  }

  onSubmit(): void {
    if (!this.isAdmin) return;
    if (this.categoriaForm.invalid) {
      this.categoriaForm.markAllAsTouched();
      return;
    }

    const formValue = this.categoriaForm.value;
    if (this.editing) {
      this.updateCategoria(formValue);
    } else {
      this.addCategoria(formValue);
    }
  }

  private addCategoria(categoria: any): void {
    const payload = { nombre: categoria.nombre };
    this.categoriaService.addCategoria(payload).subscribe({
      next: () => {
        Swal.fire('Éxito', 'Categoría agregada correctamente', 'success');
        this.loadCategorias();
        this.resetForm();
      },
      error: () => {
        Swal.fire('Error', 'No se pudo agregar la categoría', 'error');
      },
    });
  }

  private updateCategoria(categoria: any): void {
    const payload = {
      IdCategoria: Number(categoria.IdCategoria),
      nombre: categoria.nombre,
    };
    this.categoriaService.updateCategoria(payload).subscribe({
      next: () => {
        Swal.fire('Actualizado', 'Categoría actualizada correctamente', 'success');
        this.loadCategorias();
        this.resetForm();
      },
      error: () => {
        Swal.fire('Error', 'No se pudo actualizar la categoría', 'error');
      },
    });
  }

  editCategoria(categoria: any): void {
    if (!this.isAdmin) return;
    this.categoriaForm.patchValue({
      IdCategoria: categoria.id,
      nombre: categoria.nombre,
    });
    this.editing = true;
  }

  resetForm(): void {
    this.categoriaForm.reset();
    this.editing = false;
  }
}