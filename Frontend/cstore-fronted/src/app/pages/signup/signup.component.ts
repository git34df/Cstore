import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-signup',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './signup.component.html',
  styleUrls: ['./signup.component.scss'],
})
export class SignupComponent implements OnInit {
  signupForm!: FormGroup;
  loading = false;
  errorMsg = '';
  successMsg = '';

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.signupForm = this.fb.group({
      nombre: ['', Validators.required],
      numerocontacto: ['', [Validators.required, Validators.pattern('^[0-9+ ]{6,15}$')]],
      email: ['', [Validators.required, Validators.email]],
      contraseña: ['', [Validators.required, Validators.minLength(6)]],
    });
  }

  onSubmit(): void {
    if (this.signupForm.invalid) return;

    this.loading = true;
    this.errorMsg = '';
    this.successMsg = '';

    this.authService.signup(this.signupForm.value as any).subscribe({
      next: (res) => {
        this.successMsg = '✅ Usuario registrado correctamente. Ahora puedes iniciar sesión.';
        this.loading = false;

        // Redirige después de 2.5 segundos
        setTimeout(() => this.router.navigate(['/login']), 2500);
      },
      error: (err) => {
        console.error('❌ Error en registro:', err);
        this.errorMsg =
          err.error || 'Ocurrió un error al registrar el usuario. Intenta nuevamente.';
        this.loading = false;
      },
    });
  }
}
