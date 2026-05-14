import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-forgot-password',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './forgot-password.component.html',
  styleUrls: ['./forgot-password.component.scss'],
})
export class ForgotPasswordComponent implements OnInit {
  forgotForm: any;
  message = '';
  error = '';
  loading = false;

  constructor(private fb: FormBuilder, private authService: AuthService) {}

  ngOnInit(): void {
   
    this.forgotForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
    });
  }

  onSubmit(): void {
    this.message = '';
    this.error = '';

    if (this.forgotForm.invalid) return;

    const email = this.forgotForm.value.email!;
    this.loading = true;

    this.authService.forgotPassword(email).subscribe({
      next: (res) => {
        this.message = res;
        this.loading = false;
        this.forgotForm.reset();
      },
      error: (err) => {
        this.error = err.error || 'Ocurrió un error. Intenta nuevamente.';
        this.loading = false;
      },
    });
  }
}
