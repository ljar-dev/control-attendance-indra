import { Component, OnInit, OnDestroy } from '@angular/core';
import { AttendanceRecordControllerService, TodayAttendanceDto } from 'src/app/generated/api';
import { ConfirmDialogService } from 'src/app/modules/shared/services/confirm-dialog/confirm-dialog.service';
import { ToastService } from 'src/app/modules/shared/services/toast/toast.service';
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
  selector: 'app-attendance-record-page',
  templateUrl: './attendance-record-page.component.html',
  styleUrls: ['./attendance-record-page.component.css']
})
export class AttendanceRecordPageComponent implements OnInit, OnDestroy {

  // Usuario actual del token
  currentUser: UserProfile | null = null;

  // Fecha y hora actual
  currentDate: string = '';
  currentTime: string = '';
  private timeInterval: any;

  // Estado de asistencia
  hasCheckedIn: boolean = false;
  isComplete: boolean = false;

  // Registros del día
  lastRecord = {
    entry: null as string | null,
    exit: null as string | null
  };

  // Estado del botón
  isProcessing: boolean = false;
  buttonLabel: string = 'Marcar Entrada';
  loadingText: string = 'Procesando...';

  constructor(
    private attendanceRecordService: AttendanceRecordControllerService,
    private authService: AuthService,
    private confirmDialogService: ConfirmDialogService,
    private toastService: ToastService
  ) { }

  ngOnInit() {
    // Cargar usuario del token
    this.loadUserProfile();

    // Actualizar fecha y hora
    this.updateDateTime();
    this.timeInterval = setInterval(() => {
      this.updateDateTime();
    }, 1000);

    // Cargar registros de hoy del usuario autenticado
    this.loadTodayAttendance();
  }

  ngOnDestroy() {
    if (this.timeInterval) {
      clearInterval(this.timeInterval);
    }
  }

  /**
   * Carga el perfil del usuario desde el token
   */
  loadUserProfile(): void {
    this.authService.currentUser$.subscribe(user => {
      this.currentUser = user;
    });
  }

  /**
   * Actualiza fecha y hora actual
   */
  updateDateTime() {
    const now = new Date();

    this.currentDate = now.toLocaleDateString('es-ES', {
      weekday: 'long',
      day: '2-digit',
      month: 'long',
      year: 'numeric'
    });

    this.currentTime = now.toLocaleTimeString('es-ES', {
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit'
    });
  }

  /**
   * Obtiene iniciales del nombre
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
   * Obtiene el cargo/posición
   */
  getPosition(): string {
    return this.currentUser?.position || 'Empleado';
  }

  /**
   * Carga los registros del día actual del usuario autenticado
   */
  loadTodayAttendance() {
    this.attendanceRecordService.getMyTodayAttendance().subscribe({
      next: (response: TodayAttendanceDto) => {
        if (response.checkIn) {
          this.lastRecord.entry = this.formatDateTime(response.checkIn);
        } else {
          this.lastRecord.entry = null;
        }

        if (response.checkOut) {
          this.lastRecord.exit = this.formatDateTime(response.checkOut);
        } else {
          this.lastRecord.exit = null;
        }

        // Determinar si está completo (entrada Y salida)
        this.isComplete = !!(response.hasCheckIn && response.hasCheckOut);
        
        // Si tiene entrada pero NO tiene salida = está en la oficina
        this.hasCheckedIn = !!(response.hasCheckIn && !response.hasCheckOut);

        this.updateButtonState();
      },
      error: (error) => {
        console.error('Error al cargar registros de hoy:', error);

        if (error.status === 401) {
          this.toastService.error(
            'Sesión expirada',
            'Tu sesión ha expirado. Por favor inicia sesión nuevamente.'
          );
        } else if (error.status === 404) {
          this.toastService.warning(
            'Empleado no encontrado',
            'No se encontró información de empleado asociada a tu usuario'
          );
        } else {
          this.toastService.error(
            'Error al cargar',
            'No se pudo cargar la información de asistencia de hoy'
          );
        }
      }
    });
  }

  /**
   * Formatea LocalDateTime a string legible (HH:mm)
   */
  formatDateTime(dateTime: string | Date): string {
    let date: Date;

    if (typeof dateTime === 'string') {
      date = new Date(dateTime);
    } else {
      date = dateTime;
    }

    return date.toLocaleTimeString('es-ES', {
      hour: '2-digit',
      minute: '2-digit',
      hour12: false
    });
  }

  /**
   * Actualiza el estado del botón según hasCheckedIn
   */
  updateButtonState() {
    if (!this.hasCheckedIn) {
      this.buttonLabel = 'Marcar Entrada';
      this.loadingText = 'Marcando entrada...';
    } else {
      this.buttonLabel = 'Marcar Salida';
      this.loadingText = 'Marcando salida...';
    }
  }

  /**
   * Marca entrada o salida con confirmación
   */
  async markAttendance() {
    // Determinar el tipo de acción
    const isEntry = !this.hasCheckedIn;
    
    const confirmed = await this.confirmDialogService.confirm({
      header: isEntry ? 'Marcar Entrada' : 'Marcar Salida',
      message: isEntry 
        ? '¿Deseas registrar tu entrada en este momento?' 
        : '¿Deseas registrar tu salida en este momento?',
      acceptLabel: isEntry ? 'Sí, marcar entrada' : 'Sí, marcar salida',
      rejectLabel: 'Cancelar'
    });

    if (!confirmed) {
      return; // Usuario canceló
    }

    // Usuario confirmó, proceder
    this.isProcessing = true;

    if (isEntry) {
      // Marcar entrada
      this.attendanceRecordService.checkInMe().subscribe({
        next: (response) => {
          this.lastRecord.entry = this.formatDateTime(response.checkIn!);
          this.hasCheckedIn = true;
          this.updateButtonState();
          this.isProcessing = false;

          this.toastService.success(
            'Entrada registrada',
            `Tu entrada fue registrada exitosamente a las ${this.lastRecord.entry}`
          );
        },
        error: (error) => {
          console.error('Error al marcar entrada:', error);
          this.isProcessing = false;

          if (error.status === 400) {
            this.toastService.error(
              'Error al marcar entrada',
              error.error?.message || 'Ya has registrado tu entrada hoy'
            );
          } else {
            this.toastService.error(
              'Error al marcar entrada',
              'No se pudo registrar tu entrada. Intenta nuevamente.'
            );
          }
        }
      });
    } else {
      // Marcar salida
      this.attendanceRecordService.checkOutMe().subscribe({
        next: (response) => {
          this.lastRecord.exit = this.formatDateTime(response.checkOut!);
          this.hasCheckedIn = false;
          this.isComplete = true;
          this.updateButtonState();
          this.isProcessing = false;

          this.toastService.success(
            'Salida registrada',
            `Tu salida fue registrada exitosamente a las ${this.lastRecord.exit}`
          );
        },
        error: (error) => {
          console.error('Error al marcar salida:', error);
          this.isProcessing = false;

          if (error.status === 400) {
            this.toastService.error(
              'Error al marcar salida',
              error.error?.message || 'Ya has registrado tu salida hoy o no has marcado entrada'
            );
          } else {
            this.toastService.error(
              'Error al marcar salida',
              'No se pudo registrar tu salida. Intenta nuevamente.'
            );
          }
        }
      });
    }
  }
}