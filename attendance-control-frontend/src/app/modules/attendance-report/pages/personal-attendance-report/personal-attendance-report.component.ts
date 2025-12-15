import { Component, OnInit } from '@angular/core';
import { 
  AttendanceRecordControllerService,
  AttendanceRecordWithJustificationResponseDto,
  PaginationModel,
  FilterModel,
  JustificationControllerService
} from 'src/app/generated/api';
import { ToastService } from 'src/app/modules/shared/services/toast/toast.service';

interface Summary {
  totalDays: number;
  completeDays: number;
  incompleteDays: number;
  totalHours: number;
}

@Component({
  selector: 'app-personal-attendance-report',
  templateUrl: './personal-attendance-report.component.html',
  styleUrls: ['./personal-attendance-report.component.css']
})
export class PersonalAttendanceReportComponent implements OnInit {
  
  // Datos
  records: AttendanceRecordWithJustificationResponseDto[] = [];
  totalElements: number = 0;
  loading: boolean = false;

  // Filtros
  dateFrom: string = '';
  dateTo: string = '';

  // Summary
  summary: Summary = {
    totalDays: 0,
    completeDays: 0,
    incompleteDays: 0,
    totalHours: 0
  };

  // Paginación
  pageNumber: number = 0;
  rowsPerPage: number = 5;
  pageSizeOptions: number[] = [5, 10, 20, 50];

  // Modal para justificacion
  showJustificationModal = false;
  selectedAttendanceRecord: any = null;
  selectedJustification: any = null;

  constructor(
    private attendanceService: AttendanceRecordControllerService,
    private justificationService: JustificationControllerService,
    private toastService: ToastService
  ) {}

  ngOnInit(): void {
    this.initializeDateFilters();
    this.loadRecords();
  }

  initializeDateFilters(): void {
    const now = new Date();

    // Primer día del mes actual
    const firstDayOfMonth = new Date(
      now.getFullYear(),
      now.getMonth(),
      1
    );

    // Último día del mes actual
    const lastDayOfMonth = new Date(
      now.getFullYear(),
      now.getMonth() + 1,
      0
    );

    this.dateFrom = this.formatDateForInput(firstDayOfMonth);
    this.dateTo = this.formatDateForInput(lastDayOfMonth);
  }

  formatDateForInput(date: Date): string {
    const year = date.getFullYear();
    const month = (date.getMonth() + 1).toString().padStart(2, '0');
    const day = date.getDate().toString().padStart(2, '0');
    return `${year}-${month}-${day}`;
  }

  loadRecords(): void {
    this.loading = true;

    const paginationModel: PaginationModel = {
      pageNumber: this.pageNumber,
      rowsPerPage: this.rowsPerPage,
      filters: this.buildFilters(),
      sorts: [
        {
          colName: 'checkIn',
          direction: 'DESC'
        }
      ]
    };

    this.attendanceService.getMyPagination(paginationModel).subscribe({
      next: (response) => {
        this.records = response.content || [];
        this.totalElements = response.totalElements || 0;
        
        this.calculateSummary();
        this.loading = false;
      },
      error: (error) => {
        console.error('Error al cargar registros:', error);
        this.loading = false;
        
        if (error.status === 401) {
          alert('Sesión expirada. Por favor, inicia sesión nuevamente.');
        } else {
          alert('Error al cargar el historial de asistencia');
        }
      }
    });
  }

  buildFilters(): FilterModel[] {
    const filters: FilterModel[] = [];

    if (this.dateFrom) {
      filters.push({
        field: 'startDate',
        value: this.convertToISODateTime(this.dateFrom, true)
      });
    }

    if (this.dateTo) {
      filters.push({
        field: 'endDate',
        value: this.convertToISODateTime(this.dateTo, false)
      });
    }

    return filters;
  }

  convertToISODateTime(dateStr: string, startOfDay: boolean): string {
    if (startOfDay) {
      return `${dateStr}T00:00:00`;
    } else {
      return `${dateStr}T23:59:59`;
    }
  }

  applyFilters(): void {
    this.pageNumber = 0;
    this.loadRecords();
  }

  clearFilters(): void {
    this.initializeDateFilters();
    this.applyFilters();
  }

  onPageChange(pageNumber: number): void {
    this.pageNumber = pageNumber;
    this.loadRecords();
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  onPageSizeChange(event: Event): void {
    const select = event.target as HTMLSelectElement;
    this.rowsPerPage = parseInt(select.value, 10);
    this.pageNumber = 0;
    this.loadRecords();
  }

  calculateSummary(): void {
    this.summary.totalDays = this.records.length;
    
    let completeDays = 0;
    let totalMinutes = 0;

    this.records.forEach(record => {
      if (record.checkIn && record.checkOut) {
        completeDays++;
        
        const checkIn = new Date(record.checkIn);
        const checkOut = new Date(record.checkOut);
        const diffMs = checkOut.getTime() - checkIn.getTime();
        const diffMinutes = Math.floor(diffMs / 60000);
        totalMinutes += diffMinutes;
      }
    });

    this.summary.completeDays = completeDays;
    this.summary.incompleteDays = this.summary.totalDays - completeDays;
    this.summary.totalHours = Math.round(totalMinutes / 60);
  }

  /**
   * Abrir modal de justificación
   */
  openJustificationModal(record: AttendanceRecordWithJustificationResponseDto): void {
    this.selectedAttendanceRecord = {
      idAttendanceRecord: record.idAttendanceRecord,
      employeeName: record.employeeName,
      employeeCode: record.employeeCode,
      attendanceDate: record.attendanceDate,
      status: record.status
    };
    this.selectedJustification = record.justification;
    this.showJustificationModal = true;
  }

  /**
   * Cerrar modal
   */
  closeJustificationModal(): void {
    this.showJustificationModal = false;
    this.selectedAttendanceRecord = null;
    this.selectedJustification = null;
  }

  /**
   * Enviar justificación
   */
  onSubmitJustification(justificationText: string): void {
    if (!this.selectedAttendanceRecord) return;

    this.justificationService.submitJustification(
      this.selectedAttendanceRecord.idAttendanceRecord,
      { justificationText }
    ).subscribe({
      next: (response) => {

        this.toastService.success(
            'Justificación enviada',
            'Tu justificación ha sido enviada exitosamente'
          );
        this.closeJustificationModal();
        this.loadRecords();
      },
      error: (error) => {

        this.toastService.success(
            'Error al enviar la justificación',
            'Error desconocido'
          );
      }
    });
  }

  /**
   * Verificar si el registro necesita justificación
   */
  needsJustification(record: AttendanceRecordWithJustificationResponseDto | null): boolean {
    if (!record) return false;
    return record.justification !== null && record.justification !== undefined;
  }

  /**
   * Verificar si ya tiene justificación enviada
   */
  hasJustification(record: AttendanceRecordWithJustificationResponseDto | null): boolean {
    if (!record) return false;
    if (!record.justification) return false;
    return record.justification.justificationText !== null &&
          record.justification.justificationText !== undefined &&
          record.justification.justificationText.trim() !== '';
  }

  formatDate(dateTime: string | Date | undefined): string {
    if (!dateTime) return '-';
    
    const date = new Date(dateTime);
    return date.toLocaleDateString('es-ES', {
      weekday: 'long',
      day: '2-digit',
      month: 'long',
      year: 'numeric'
    });
  }

  getDayOfMonth(dateTime: string | Date | undefined): string {
    if (!dateTime) return '-';
    
    const date = new Date(dateTime);
    return date.getDate().toString();
  }

  getMonthShort(dateTime: string | Date | undefined): string {
    if (!dateTime) return '-';
    
    const date = new Date(dateTime);
    const months = ['Ene', 'Feb', 'Mar', 'Abr', 'May', 'Jun', 
                   'Jul', 'Ago', 'Sep', 'Oct', 'Nov', 'Dic'];
    return months[date.getMonth()];
  }

  getDayName(dateTime: string | Date | undefined): string {
    if (!dateTime) return '-';
    
    const date = new Date(dateTime);
    const days = ['Dom', 'Lun', 'Mar', 'Mié', 'Jue', 'Vie', 'Sáb'];
    return days[date.getDay()];
  }

  formatTime(dateTime: string | Date | undefined): string {
    if (!dateTime) return '-';
    
    const date = new Date(dateTime);
    return date.toLocaleTimeString('es-ES', {
      hour: '2-digit',
      minute: '2-digit',
      hour12: false
    });
  }

  calculateHours(checkIn: string | Date | undefined, checkOut: string | Date | undefined): string {
    if (!checkIn || !checkOut) return '-';
    
    const start = new Date(checkIn);
    const end = new Date(checkOut);
    
    const diffMs = end.getTime() - start.getTime();
    const hours = Math.floor(diffMs / 3600000);
    const minutes = Math.floor((diffMs % 3600000) / 60000);
    
    return `${hours}h ${minutes}m`;
  }

  getStatus(record: AttendanceRecordWithJustificationResponseDto): string {
    if (record.checkIn && record.checkOut) {
      return 'Completo';
    } else if (record.checkIn && !record.checkOut) {
      return 'Incompleto';
    } else {
      return 'Sin entrada';
    }
  }

  getStatusClass(record: AttendanceRecordWithJustificationResponseDto): string {
    if (record.checkIn && record.checkOut) {
      return 'status-complete';
    } else {
      return 'status-incomplete';
    }
  }

  getTotalPages(): number {
    return Math.ceil(this.totalElements / this.rowsPerPage);
  }

  getRecordsRange(): string {
    const start = this.pageNumber * this.rowsPerPage + 1;
    const end = Math.min((this.pageNumber + 1) * this.rowsPerPage, this.totalElements);
    return `${start}-${end} de ${this.totalElements}`;
  }

  hasPreviousPage(): boolean {
    return this.pageNumber > 0;
  }

  hasNextPage(): boolean {
    return (this.pageNumber + 1) < this.getTotalPages();
  }

  previousPage(): void {
    if (this.hasPreviousPage()) {
      this.onPageChange(this.pageNumber - 1);
    }
  }

  nextPage(): void {
    if (this.hasNextPage()) {
      this.onPageChange(this.pageNumber + 1);
    }
  }
}