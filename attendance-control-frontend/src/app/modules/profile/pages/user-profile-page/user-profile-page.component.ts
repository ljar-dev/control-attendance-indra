import { Component, OnInit } from '@angular/core';
import { AbstractControl, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthControllerService, ChangePasswordRequestDto } from 'src/app/generated/api';
import { AuthService } from 'src/app/services/auth.service';

interface UserProfile {
  username: string;
  roles: string[];
  employeeId?: number;
  employeeCode?: string;
  firstName?: string;
  lastName?: string;
  department?: string;
  position?: string;
}

@Component({
  selector: 'app-user-profile-page',
  templateUrl: './user-profile-page.component.html',
  styleUrls: ['./user-profile-page.component.css']
})
export class UserProfilePageComponent implements OnInit {
  
  // Usuario actual (solo del token)
  currentUser: UserProfile | null = null;
  
  // Formulario de cambio de contraseña
  passwordForm: FormGroup;
  
  // Estado
  loading = false;
  showPasswordModal = false;
  submittingPassword = false;
  showCurrentPassword = false;
  showNewPassword = false;
  showConfirmPassword = false;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private authServiceController: AuthControllerService,
    private router: Router
  ) {
    // Inicializar formulario de contraseña
    this.passwordForm = this.fb.group(
      {
        currentPassword: ['', [Validators.required]],
        newPassword: ['', [Validators.required]],
        confirmPassword: ['', [Validators.required]]
      },
      { validators: this.passwordMatchValidator }
    );
  }

  ngOnInit(): void {
    this.loadUserProfile();
  }

  /**
   * Validador personalizado para confirmar contraseñas
   */
  passwordMatchValidator(form: AbstractControl) {
    const newPassword = form.get('newPassword')?.value;
    const confirmPassword = form.get('confirmPassword')?.value;

    return newPassword === confirmPassword ? null : { passwordMismatch: true };
  }
  /**
   * Carga el perfil del usuario desde el token
   */
  loadUserProfile(): void {
    this.loading = true;

    this.authService.currentUser$.subscribe(user => {
      this.currentUser = user;
      this.loading = false;
    });
  }

  /**
   * Obtiene las iniciales del nombre
   */
  getInitials(): string {
    if (!this.currentUser) return '??';
    
    const firstName = this.currentUser.firstName || '';
    const lastName = this.currentUser.lastName || '';
    
    if (!firstName && !lastName) {
      return this.currentUser.username.substring(0, 2).toUpperCase();
    }
    
    return `${firstName.charAt(0)}${lastName.charAt(0)}`.toUpperCase();
  }

  /**
   * Obtiene el nombre completo
   */
  getFullName(): string {
    if (!this.currentUser) return 'Usuario';
    
    const fullName = `${this.currentUser.firstName || ''} ${this.currentUser.lastName || ''}`.trim();
    
    return fullName || this.currentUser.username;
  }

  /**
   * Obtiene los roles en formato legible
   */
  getRolesDisplay(): string[] {
    if (!this.currentUser?.roles) return [];
    
    return this.currentUser.roles.map(role => {
      return role.replace('ROLE_', '').toLowerCase()
        .split('_')
        .map(word => word.charAt(0).toUpperCase() + word.slice(1))
        .join(' ');
    });
  }

  /**
   * Abre el modal de cambio de contraseña
   */
  openPasswordModal(): void {
    this.showPasswordModal = true;
    this.passwordForm.reset();
  }

  /**
   * Cierra el modal de cambio de contraseña
   */
  closePasswordModal(): void {
    this.showPasswordModal = false;
    this.passwordForm.reset();
    this.showCurrentPassword = false;
    this.showNewPassword = false;
    this.showConfirmPassword = false;
  }

  /**
   * Cambia la contraseña
   */
  changePassword() {
  if (this.passwordForm.invalid) {
    this.passwordForm.markAllAsTouched();
    return;
  }

  this.submittingPassword = true;

    const payload = {
      oldPassword: this.passwordForm.value.currentPassword,
      newPassword: this.passwordForm.value.newPassword,
      confirmPassword: this.passwordForm.value.confirmPassword
    };

    this.authServiceController.changePassword(payload).subscribe({
      next: () => {
        this.submittingPassword = false;

        this.passwordForm.reset();
        this.closePasswordModal(); // ✅ cerrar modal como antes

        this.authService.logout(); // elimina token / sesión
        this.router.navigate(['auth/login']);

        alert('Contraseña cambiada correctamente. Vuelve a iniciar sesión.');
      },
      error: () => {
        this.submittingPassword = false;
      }
    });
  }

  /**
   * Alterna la visibilidad de la contraseña
   */
  togglePasswordVisibility(field: 'current' | 'new' | 'confirm'): void {
    switch (field) {
      case 'current':
        this.showCurrentPassword = !this.showCurrentPassword;
        break;
      case 'new':
        this.showNewPassword = !this.showNewPassword;
        break;
      case 'confirm':
        this.showConfirmPassword = !this.showConfirmPassword;
        break;
    }
  }

  /**
   * Verifica si un campo del formulario tiene error
   */
  hasPasswordError(fieldName: string): boolean {
    const field = this.passwordForm.get(fieldName);
    return !!(field && field.invalid && field.touched);
  }

  /**
   * Obtiene el mensaje de error de un campo
   */
  getPasswordErrorMessage(fieldName: string): string {
    const field = this.passwordForm.get(fieldName);
    
    if (field?.errors?.['required']) {
      return 'Este campo es obligatorio';
    }
    
    if (field?.errors?.['minlength']) {
      return 'La contraseña debe tener al menos 6 caracteres';
    }
    
    if (fieldName === 'confirmPassword' && this.passwordForm.errors?.['passwordMismatch']) {
      return 'Las contraseñas no coinciden';
    }
    
    return '';
  }
}
