import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  ReactiveFormsModule,
  FormBuilder,
  FormArray,
  FormGroup,
  Validators,
} from '@angular/forms';
import { RouterLink } from '@angular/router';
import Swal from 'sweetalert2';
import { BillService } from '../../core/services/bill.service';
import { ProductoService } from '../../core/services/producto.service';
import { ClienteService } from '../../core/services/cliente.service';

@Component({
  selector: 'app-bill',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule,RouterLink],
  templateUrl: './ordenes.component.html',
  styleUrls: ['./ordenes.component.scss'],
})
export class BillComponent implements OnInit {
  facturaForm!: FormGroup;
  facturas: any[] = [];
  productosCatalogo: any[] = [];
  clientes: any[] = [];
  loading = false;

  constructor(
    private fb: FormBuilder,
    private billService: BillService,
    private productoService: ProductoService,
    private clienteService: ClienteService,
  ) {}

  ngOnInit(): void {
    this.initForm();
    this.loadBills();
    this.loadProductos();
    this.loadClientes();
  }

  // ── Cargar productos ────────────────────────────────────
  loadProductos() {
    this.productoService.getProductos().subscribe({
      next: (res: any) => {
        this.productosCatalogo = Array.isArray(res) ? res : (res.data ?? []);
      },
      error: (err) => console.error('Error cargando productos', err),
    });
  }

  loadClientes() {
    this.clienteService.getAllClientes().subscribe({
      next: (data) => (this.clientes = data),
      error: () => console.error('No se pudieron cargar los clientes'),
    });
  }

  // ── Inicializar formulario ──────────────────────────────
  initForm() {
    this.facturaForm = this.fb.group({
      // Datos del cliente
      nombre: ['', Validators.required],
      numerocontacto: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      // Campos SUNAT — obligatorios para factura
      ruc_cliente: ['', [Validators.required, Validators.pattern(/^\d{11}$/)]],
      razon_social: ['', Validators.required],
      direccion_cliente: [''],
      // Pago
      metodo_pago: ['', Validators.required],
      // Totales (calculados)
      subtotal: [{ value: 0, disabled: true }],
      igv: [{ value: 0, disabled: true }],
      total: [{ value: 0, disabled: true }],
      // Detalle
      detalleproducto: this.fb.array([], Validators.required),
    });
  }

  get detalleProducto(): FormArray {
    return this.facturaForm.get('detalleproducto') as FormArray;
  }

  // ── Agregar fila de producto ────────────────────────────
  addProducto() {
    const grupo = this.fb.group({
      nombre: ['', Validators.required],
      categoria: ['', Validators.required],
      cantidad: [1, [Validators.required, Validators.min(1)]],
      precio: [{ value: 0, disabled: false }, Validators.required],
      total: [{ value: 0, disabled: true }],
    });

    grupo.get('nombre')?.valueChanges.subscribe((nombre) => {
      const p = this.productosCatalogo.find((x) => x.nombre === nombre);
      if (p) {
        grupo.get('categoria')?.setValue(p.categoriaName ?? p.categoria ?? '');
        grupo.get('precio')?.setValue(p.precio);
        this.calcularFila(grupo);
      }
    });

    grupo.get('cantidad')?.valueChanges.subscribe(() => this.calcularFila(grupo));
    grupo.get('precio')?.valueChanges.subscribe(() => this.calcularFila(grupo));

    this.detalleProducto.push(grupo);
  }

  eliminarProducto(index: number) {
    this.detalleProducto.removeAt(index);
    this.recalcularTotales();
  }

  // ── Calcular fila ───────────────────────────────────────
  calcularFila(grupo: FormGroup) {
    const cantidad = Number(grupo.get('cantidad')?.value || 0);
    const precio = Number(grupo.get('precio')?.value || 0);
    grupo.get('total')?.setValue(cantidad * precio, { emitEvent: false });
    this.recalcularTotales();
  }

  // ── Recalcular totales con IGV 18% ─────────────────────
  recalcularTotales() {
    const totalConIgv = this.detalleProducto.controls
      .map((c) => Number(c.get('total')?.value || 0))
      .reduce((a, b) => a + b, 0);

    const subtotal = Math.round((totalConIgv / 1.18) * 100) / 100;
    const igv = Math.round((totalConIgv - subtotal) * 100) / 100;

    this.facturaForm.get('subtotal')?.setValue(subtotal.toFixed(2), { emitEvent: false });
    this.facturaForm.get('igv')?.setValue(igv.toFixed(2), { emitEvent: false });
    this.facturaForm.get('total')?.setValue(totalConIgv.toFixed(2), { emitEvent: false });
  }

  // ── Seleccionar producto del select ────────────────────
  onSelectProducto(i: number) {
    const grupo = this.detalleProducto.at(i) as FormGroup;
    const nombre = grupo.get('nombre')?.value;
    const prod = this.productosCatalogo.find((p) => p.nombre === nombre);
    if (!prod) return;

    const cantidad = grupo.get('cantidad')?.value || 1;
    grupo.patchValue({
      categoria: prod.categoriaName ?? prod.categoria ?? '',
      precio: prod.precio,
      total: prod.precio * cantidad,
    });
    this.recalcularTotales();
  }

  // ── Generar factura ─────────────────────────────────────
  generateFactura() {
    if (this.facturaForm.invalid || this.detalleProducto.length === 0) {
      this.facturaForm.markAllAsTouched();
      Swal.fire('Formulario incompleto', 'Completa todos los campos requeridos', 'warning');
      return;
    }

    const v = this.facturaForm.getRawValue();

    const payload = {
      name: v.nombre,
      numero_contacto: v.numerocontacto,
      email: v.email,
      ruc_cliente: v.ruc_cliente,
      razon_social: v.razon_social,
      direccion_cliente: v.direccion_cliente,
      metodo_pago: v.metodo_pago,
      total: v.total,
      detalleproducto: JSON.stringify(v.detalleproducto),
      isGenerate: true,
      uuid: '',
    };

    this.loading = true;
    this.billService.generateReport(payload).subscribe({
      next: (res: any) => {
        this.loading = false;
        this.descargarPdf(res.uuid);
        Swal.fire('¡Factura generada!', `Serie: F001 | UUID: ${res.uuid}`, 'success');
        this.initForm();
        this.loadBills();
      },
      error: (err) => {
        this.loading = false;
        console.error(err);
        Swal.fire('Error', 'No se pudo generar la factura', 'error');
      },
    });
  }

  // ── Descargar PDF ───────────────────────────────────────
  descargarPdf(uuid: string) {
    this.billService.getPdf(uuid).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(new Blob([blob], { type: 'application/pdf' }));
        const link = document.createElement('a');
        link.href = url;
        link.download = `Factura-${uuid}.pdf`;
        link.click();
        window.URL.revokeObjectURL(url);
      },
      error: (err) => console.error('Error descargando PDF', err),
    });
  }

  // ── Cargar facturas ─────────────────────────────────────
  loadBills() {
    this.billService.getBills().subscribe({
      next: (res) => (this.facturas = res),
      error: (err) => console.error(err),
    });
  }

  // ── Eliminar factura ────────────────────────────────────
  borrarFactura(id: number) {
    Swal.fire({
      title: '¿Eliminar factura?',
      text: 'Esta acción no se puede deshacer',
      icon: 'warning',
      showCancelButton: true,
      confirmButtonColor: '#ef4444',
      cancelButtonText: 'Cancelar',
      confirmButtonText: 'Sí, eliminar',
    }).then((r) => {
      if (r.isConfirmed) {
        this.billService.deleteBill(id).subscribe({
          next: () => {
            Swal.fire('Eliminado', 'Factura eliminada correctamente', 'success');
            this.loadBills();
          },
          error: () => Swal.fire('Error', 'No se pudo eliminar', 'error'),
        });
      }
    });
  }

  onEmailChange() {
    const email = this.facturaForm.get('email')?.value?.toLowerCase().trim();
    if (!email) return;

    const cliente = this.clientes.find((c: any) => c.email?.toLowerCase() === email);

    if (cliente) {
      this.facturaForm.patchValue({
        nombre: cliente.nombre || '',
        ruc_cliente: cliente.ruc || '',
        razon_social: cliente.razonSocial || '',
        numerocontacto: cliente.telefono || '',
        direccion_cliente: cliente.direccion || '',
      });
    }
  }

  // ── Helpers ─────────────────────────────────────────────
  isInvalid(field: string): boolean {
    const ctrl = this.facturaForm.get(field);
    return !!(ctrl?.invalid && ctrl?.touched);
  }
}