import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { LoginRequestDto } from 'src/app/generated/api';
import { ToastService } from 'src/app/modules/shared/services/toast/toast.service';
import { AuthService } from 'src/app/services/auth.service';

@Component({
  selector: 'app-login-page',
  templateUrl: './login-page.component.html',
  styleUrls: ['./login-page.component.css']
})
export class LoginPageComponent {
  loginForm: FormGroup;
  loading = false;
  errorMessage = '';

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router,
    private toastService: ToastService
  ) {
    this.loginForm = this.fb.group({
      username: ['', [Validators.required, Validators.minLength(3)]],
      password: ['', [Validators.required, Validators.minLength(5)]]
    });
  }

  ngOnInit(): void {
    // Redirigir si ya está autenticado
    if (this.authService.isAuthenticated()) {
      // Verificar si debe cambiar contraseña
      if (this.authService.mustChangePassword()) {
        this.router.navigate(['/first-login']);
      } else {
        this.router.navigate(['/home/attendance-record']);
      }
    }
  }

  onSubmit(): void {
    if (this.loginForm.invalid) {
      this.loginForm.markAllAsTouched();
      return;
    }

    this.loading = true;
    this.errorMessage = '';

    const credentials: LoginRequestDto = {
      username: this.loginForm.value.username,
      password: this.loginForm.value.password
    };

    this.authService.login(credentials).subscribe({
      next: (response) => {
        const currentUser = this.authService.getCurrentUser();
        
        // Verificar si debe cambiar contraseña
        if (currentUser?.mustChangePassword) {
          this.toastService.info(
            'Primer inicio de sesión',
            'Debes cambiar tu contraseña para continuar'
          );
          this.router.navigate(['auth/first-login']);
        } else {
          this.toastService.success(
            'Inicio de sesión exitoso',
            `Bienvenido de vuelta, ${currentUser?.firstName || this.loginForm.value.username}`
          );
          this.router.navigate(['home/attendance-record']);
        }
      },
      error: (error) => {
        console.error('Error en login:', error);
        this.loading = false;
        
        if (error.status === 401) {
          this.errorMessage = 'Usuario o contraseña incorrectos';
        } else if (error.status === 0) {
          this.errorMessage = 'No se puede conectar al servidor';
        } else {
          this.errorMessage = 'Error al iniciar sesión';
        }
      },
      complete: () => {
        this.loading = false;
      }
    });
  }

  // Getters para acceder a los campos en el template
  get username() {
    return this.loginForm.get('username');
  }

  get password() {
    return this.loginForm.get('password');
  }
}