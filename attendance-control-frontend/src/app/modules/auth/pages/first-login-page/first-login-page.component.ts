import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from 'src/app/services/auth.service';
import { ToastService } from 'src/app/modules/shared/services/toast/toast.service';
import { AuthControllerService } from 'src/app/generated/api';

@Component({
  selector: 'app-first-login-page',
  templateUrl: './first-login-page.component.html',
  styleUrls: ['./first-login-page.component.css']
})
export class FirstLoginPageComponent {

  passwordForm: FormGroup;
  loading = false;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router,
    private toastService: ToastService,
    private authControllerService: AuthControllerService
  ) {
    this.passwordForm = this.fb.group(
      {
        newPassword: ['', [Validators.required, Validators.minLength(6)]],
        confirmPassword: ['', [Validators.required]]
      },
      { validators: this.passwordMatchValidator }
    );
  }

  passwordMatchValidator(form: FormGroup) {
    const newPassword = form.get('newPassword')?.value;
    const confirmPassword = form.get('confirmPassword')?.value;
    return newPassword === confirmPassword ? null : { passwordMismatch: true };
  }

  submit(): void {
    if (this.passwordForm.invalid) {
      this.passwordForm.markAllAsTouched();
      return;
    }

    this.loading = true;

    this.authControllerService.changePasswordFirstLogin({
      newPassword: this.passwordForm.value.newPassword
    }).subscribe({
      next: (response) => {
        // Actualizar el token en el storage
        this.authService.updateToken(response.token!);
        
        this.toastService.success(
          'Contraseña actualizada exitosamente',
          'Ahora puedes usar el sistema'
        );

        // Redirigir al dashboard
        this.router.navigate(['/dashboard']);
      },
      error: () => {
        this.loading = false;
        this.toastService.error('Error', 'Error al cambiar contraseña');
      }
    });
  }
}