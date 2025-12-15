import { Component, OnInit } from '@angular/core';
import { 
  AttendanceRecordControllerService,
  EmployeeControllerService,
  AttendanceRecordWithJustificationResponseDto,
  EmployeeListResponseDto,
  PaginationModel,
  FilterModel,
  JustificationControllerService
} from 'src/app/generated/api';
import { ToastService } from 'src/app/modules/shared/services/toast/toast.service';

interface EmployeeOption {
  label: string;
  value: number;
  employee: EmployeeListResponseDto;
}

interface Summary {
  totalDays: number;
  completeDays: number;
  incompleteDays: number;
  totalHours: number;
}

@Component({
  selector: 'app-attendance-report-page',
  templateUrl: './attendance-report-page.component.html',
  styleUrls: ['./attendance-report-page.component.css']
})
export class AttendanceReportPageComponent implements OnInit {
  
  // Empleados
  employees: EmployeeOption[] = [];
  selectedEmployeeId: number | null = null;
  selectedEmployeeName: string = '';
  loadingEmployees: boolean = false;

  // Registros de asistencia
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
  currentPage: number = 0;
  pageSize: number = 10;
  pageSizeOptions: number[] = [5, 10, 20, 50];

  constructor(
    private attendanceService: AttendanceRecordControllerService,
    private employeeService: EmployeeControllerService,
    private toastService: ToastService,
    private justificationService: JustificationControllerService
  ) {}

  ngOnInit(): void {
    this.initializeDateFilters();
    this.loadEmployees();
  }

  /**
   * Inicializa los filtros de fecha (último mes por defecto)
   */
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

  /**
   * Formatea una fecha para el input type="date" (yyyy-MM-dd)
   */
  formatDateForInput(date: Date): string {
    const year = date.getFullYear();
    const month = (date.getMonth() + 1).toString().padStart(2, '0');
    const day = date.getDate().toString().padStart(2, '0');
    return `${year}-${month}-${day}`;
  }

  /**
   * Carga la lista de empleados
   */
  loadEmployees(): void {
    this.loadingEmployees = true;

    const paginationModel: PaginationModel = {
      pageNumber: 0,
      rowsPerPage: 1000, // Cargar todos los empleados activos
      filters: [
        {
          field: 'enabled',
          value: 'true'
        }
      ],
      sorts: [
        {
          colName: 'firstName',
          direction: 'ASC'
        }
      ]
    };

    this.employeeService.getPaginationEmployees(paginationModel).subscribe({
      next: (response) => {
        console.log('Empleados cargados:', response);
        
        // Mapear a opciones para el select
        this.employees = (response.content || []).map(emp => ({
          label: `${emp.firstName} ${emp.lastName} - ${emp.employeeCode}`,
          value: emp.idEmployee!,
          employee: emp
        }));

        this.loadingEmployees = false;
      },
      error: (error) => {
        console.error('Error al cargar empleados:', error);
        this.loadingEmployees = false;
        alert('Error al cargar la lista de empleados');
      }
    });
  }

  /**
   * Maneja la selección de empleado
   */
  onEmployeeSelected(employee: EmployeeOption): void {
    console.log('Empleado seleccionado:', employee);
    
    if (employee && employee.value) {
      this.selectedEmployeeId = employee.value;
      this.selectedEmployeeName = employee.label;
      this.currentPage = 0;
      this.loadRecords();
    }
  }

  updateJustificationApproval(justificationId: number, approved: boolean): void {
    const approvalRequest = { approved };
    
    this.justificationService.updateApprovalStatus(justificationId, approvalRequest).subscribe({
      next: (response) => {
        this.toastService.success(
          'Estado actualizado',
          `Justificación ${approved ? 'aprobada' : 'rechazada'} correctamente`
        );
        this.loadRecords();
      },
      error: (error) => {
        console.error('Error updating approval:', error);
        this.toastService.error(
          'Error al actualizar',
          error.error?.message || 'No se pudo actualizar el estado de la justificación'
        );
      }
    });
  }

  /**
   * Maneja el cambio de valor del select
   */
  onEmployeeValueChange(employeeId: number): void {
    console.log('ID seleccionado:', employeeId);
    this.selectedEmployeeId = employeeId;
  }

  /**
   * Carga los registros de asistencia del empleado seleccionado
   */
  loadRecords(): void {
    if (!this.selectedEmployeeId) {
      return;
    }

    this.loading = true;

    // Construir el modelo de paginación
    const paginationModel: PaginationModel = {
      pageNumber: this.currentPage, // Backend usa índice 0
      rowsPerPage: this.pageSize,
      filters: this.buildFilters(),
      sorts: [
        {
          colName: 'checkIn',
          direction: 'DESC'
        }
      ]
    };

    console.log('Paginación enviada:', paginationModel);

    // Llamar al endpoint de paginación de asistencia
    // Asumiendo que tienes un método getPaginationByEmployee o similar
    // Si no existe, necesitarás crearlo en el backend
    this.attendanceService.getPaginationRecordAttendance(paginationModel).subscribe({
      next: (response) => {
        console.log('Respuesta recibida:', response);
        
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

  /**
   * Construye los filtros para el backend
   */
  buildFilters(): FilterModel[] {
    const filters: FilterModel[] = [];

    // Filtro por empleado
    if (this.selectedEmployeeId) {
      filters.push({
        field: 'employeeId',
        value: this.selectedEmployeeId.toString()
      });
    }

    // Filtro por fecha desde
    if (this.dateFrom) {
      filters.push({
        field: 'startDate',
        value: this.convertToISODateTime(this.dateFrom, true)
      });
    }

    // Filtro por fecha hasta
    if (this.dateTo) {
      filters.push({
        field: 'endDate',
        value: this.convertToISODateTime(this.dateTo, false)
      });
    }

    return filters;
  }

  /**
   * Convierte una fecha string (yyyy-MM-dd) a ISO DateTime
   */
  convertToISODateTime(dateStr: string, startOfDay: boolean): string {
    if (startOfDay) {
      return `${dateStr}T00:00:00`;
    } else {
      return `${dateStr}T23:59:59`;
    }
  }

  /**
   * Aplica los filtros y recarga los datos
   */
  applyFilters(): void {
    if (!this.selectedEmployeeId) {
      alert('Por favor, selecciona un empleado');
      return;
    }

    this.currentPage = 0;
    this.loadRecords();
  }

  /**
   * Limpia los filtros
   */
  clearFilters(): void {
    this.initializeDateFilters();
    if (this.selectedEmployeeId) {
      this.applyFilters();
    }
  }

  /**
   * Limpia la selección de empleado
   */
  clearSelection(): void {
    this.selectedEmployeeId = null;
    this.selectedEmployeeName = '';
    this.records = [];
    this.totalElements = 0;
    this.resetSummary();
  }

  /**
   * Maneja el cambio de página desde el PaginatorComponent
   */
  onPageChange(event: any): void {
    this.currentPage = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadRecords();
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  /**
   * Calcula el resumen de asistencia
   */
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
   * Resetea el resumen
   */
  resetSummary(): void {
    this.summary = {
      totalDays: 0,
      completeDays: 0,
      incompleteDays: 0,
      totalHours: 0
    };
  }

  /**
   * Formatea fecha para mostrar
   */
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

  /**
   * Obtiene solo el día del mes
   */
  getDayOfMonth(dateTime: string | Date | undefined): string {
    if (!dateTime) return '-';
    
    const date = new Date(dateTime);
    return date.getDate().toString();
  }

  /**
   * Obtiene el mes abreviado
   */
  getMonthShort(dateTime: string | Date | undefined): string {
    if (!dateTime) return '-';
    
    const date = new Date(dateTime);
    const months = ['Ene', 'Feb', 'Mar', 'Abr', 'May', 'Jun', 
                   'Jul', 'Ago', 'Sep', 'Oct', 'Nov', 'Dic'];
    return months[date.getMonth()];
  }

  /**
   * Obtiene el nombre del día
   */
  getDayName(dateTime: string | Date | undefined): string {
    if (!dateTime) return '-';
    
    const date = new Date(dateTime);
    const days = ['Dom', 'Lun', 'Mar', 'Mié', 'Jue', 'Vie', 'Sáb'];
    return days[date.getDay()];
  }

  /**
   * Formatea hora para mostrar (HH:mm)
   */
  formatTime(dateTime: string | Date | undefined): string {
    if (!dateTime) return '-';
    
    const date = new Date(dateTime);
    return date.toLocaleTimeString('es-ES', {
      hour: '2-digit',
      minute: '2-digit',
      hour12: false
    });
  }

  /**
   * Calcula las horas trabajadas
   */
  calculateHours(checkIn: string | Date | undefined, checkOut: string | Date | undefined): string {
    if (!checkIn || !checkOut) return '-';
    
    const start = new Date(checkIn);
    const end = new Date(checkOut);
    
    const diffMs = end.getTime() - start.getTime();
    const hours = Math.floor(diffMs / 3600000);
    const minutes = Math.floor((diffMs % 3600000) / 60000);
    
    return `${hours}h ${minutes}m`;
  }

  /**
   * Obtiene el estado de un registro
   */
  getStatus(record: AttendanceRecordWithJustificationResponseDto): string {
    if (record.checkIn && record.checkOut) {
      return 'Completo';
    } else if (record.checkIn && !record.checkOut) {
      return 'Incompleto';
    } else {
      return 'Sin entrada';
    }
  }

  /**
   * Obtiene la clase CSS del estado
   */
  getStatusClass(record: AttendanceRecordWithJustificationResponseDto): string {
    if (record.checkIn && record.checkOut) {
      return 'status-complete';
    } else {
      return 'status-incomplete';
    }
  }

  // Modal de justificación
  showJustificationModal = false;
  selectedAttendanceRecord: any = null;
  selectedJustification: any = null;

  /**
   * Abrir modal de justificación (solo lectura)
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
   * Verificar si el registro tiene justificación
   */
  hasJustification(record: AttendanceRecordWithJustificationResponseDto | null): boolean {
    if (!record) return false;
    if (!record.justification) return false;
    return record.justification.justificationText !== null &&
          record.justification.justificationText !== undefined &&
          record.justification.justificationText.trim() !== '';
  }

  getRecordsRange(): string {
    const start = this.currentPage * this.pageSize + 1;
    const end = Math.min((this.currentPage + 1) * this.pageSize, this.totalElements);
    return `${start}-${end} de ${this.totalElements}`;
  }

  onPageSizeChange(event: Event): void {
    const select = event.target as HTMLSelectElement;
    this.pageSize = parseInt(select.value, 10);
    this.currentPage = 0;
    this.loadRecords();
  }

  hasPreviousPage(): boolean {
    return this.currentPage > 0;
  }

  hasNextPage(): boolean {
    return (this.currentPage + 1) < this.getTotalPages();
  }

  previousPage(): void {
    if (this.hasPreviousPage()) {
      this.onPageChange(this.currentPage - 1);
    }
  }

  nextPage(): void {
    if (this.hasNextPage()) {
      this.onPageChange(this.currentPage + 1);
    }
  }

  getTotalPages(): number {
    return Math.ceil(this.totalElements / this.pageSize);
  }
}