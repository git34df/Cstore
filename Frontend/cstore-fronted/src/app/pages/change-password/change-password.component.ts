import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-change-password',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './change-password.component.html',
  styleUrls: ['./change-password.component.scss'],
})
export class ChangePasswordComponent implements OnInit {
  passwordForm!: FormGroup;
  loading = false;
  message = '';
  error = '';

  constructor(
    private fb: FormBuilder,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    // ✅ Inicialización en ngOnInit para evitar el error del fb
    this.passwordForm = this.fb.group({
      oldPassword: ['', Validators.required],
      newPassword: ['', [Validators.required, Validators.minLength(6)]],
    });
  }

  onSubmit(): void {
    this.error = '';
    this.message = '';

    if (this.passwordForm.invalid) return;

    const { oldPassword, newPassword } = this.passwordForm.value;

    this.loading = true;

    // ✅ Solo dos parámetros enviados al backend
    this.authService.changePassword({ oldPassword, newPassword }).subscribe({
      next: (res) => {
        this.message = 'Contraseña actualizada correctamente.';
        this.loading = false;
        this.passwordForm.reset();
      },
      error: (err) => {
        this.error = err?.error || 'Error al cambiar la contraseña.';
        this.loading = false;
      },
    });
  }
}
