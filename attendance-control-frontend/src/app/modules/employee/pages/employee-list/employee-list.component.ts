import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import {
  EmployeeControllerService,
  EmployeeListResponseDto,
  PaginationModel,
  FilterModel
} from 'src/app/generated/api';
import { ConfirmDialogService } from 'src/app/modules/shared/services/confirm-dialog/confirm-dialog.service';
import { ToastService } from 'src/app/modules/shared/services/toast/toast.service';

interface EmployeeStats {
  total: number;
  active: number;
  inactive: number;
}

@Component({
  selector: 'app-employee-list',
  templateUrl: './employee-list.component.html',
  styleUrls: ['./employee-list.component.css']
})
export class EmployeeListComponent implements OnInit {

  // Empleados
  employees: EmployeeListResponseDto[] = [];
  totalElements: number = 0;
  loading: boolean = false;

  // Filtros
  searchQuery: string = '';
  departmentFilter: string = '';
  statusFilter: string = 'all'; // 'all', 'active', 'inactive'

  // Departamentos únicos
  departments: string[] = [];

  // Estadísticas (calculadas desde la página actual)
  stats: EmployeeStats = {
    total: 0,
    active: 0,
    inactive: 0
  };

  // Paginación
  pageNumber: number = 0;
  rowsPerPage: number = 5;
  pageSizeOptions: number[] = [5, 10, 20, 50];

  constructor(
    private employeeService: EmployeeControllerService,
    private router: Router,
    private confirmDialogService: ConfirmDialogService,
    private toastService: ToastService
  ) { }

  ngOnInit(): void {
    //  Solo una llamada al cargar
    this.loadEmployees();
  }

  /**
   * Carga los empleados con paginación y filtros
   */
  loadEmployees(): void {
    this.loading = true;

    const paginationModel: PaginationModel = {
      pageNumber: this.pageNumber,
      rowsPerPage: this.rowsPerPage,
      filters: this.buildFilters(),
      sorts: [
        {
          colName: 'employeeId',
          direction: 'DESC'
        }
      ]
    };

    this.employeeService.getPaginationEmployees(paginationModel).subscribe({
      next: (response) => {
        this.employees = response.content || [];
        this.totalElements = response.totalElements || 0;

        //  Calcular stats desde los empleados de la página actual
        this.calculateStats();

        this.extractDepartments();
        this.loading = false;
      },
      error: (error) => {
        console.error('Error al cargar empleados:', error);
        this.loading = false;

        this.toastService.error(
          'Error al cargar',
          'No se pudieron cargar los empleados'
        );
      }
    });
  }

  /**
   *  OPTIMIZADO - Calcula estadísticas desde los empleados actuales
   */
  calculateStats(): void {
    // Total viene del backend (todos los registros, no solo la página actual)
    this.stats.total = this.totalElements;

    // Active e Inactive los calculamos desde la página actual (aproximado)
    // Si quieres stats exactas, necesitas un endpoint dedicado en el backend
    this.stats.active = this.employees.filter(e => e.enabled).length;
    this.stats.inactive = this.employees.filter(e => !e.enabled).length;

  }

  /**
   * Construye los filtros para el backend
   */
  buildFilters(): FilterModel[] {
    const filters: FilterModel[] = [];

    if (this.searchQuery.trim()) {
      filters.push({
        field: 'search',
        value: this.searchQuery.trim()
      });
    }

    if (this.departmentFilter) {
      filters.push({
        field: 'department',
        value: this.departmentFilter
      });
    }

    if (this.statusFilter !== 'all') {
      filters.push({
        field: 'enabled',
        value: this.statusFilter === 'active' ? 'true' : 'false'
      });
    }

    return filters;
  }

  /**
   * Extrae departamentos únicos
   */
  extractDepartments(): void {
    const depts = new Set<string>();
    this.employees.forEach(emp => {
      if (emp.department) {
        depts.add(emp.department);
      }
    });
    this.departments = Array.from(depts).sort();
  }

  /**
   * Aplica los filtros
   */
  applyFilters(): void {
    this.pageNumber = 0; //  Resetear a página 0 (no 1)
    this.loadEmployees();
  }

  /**
   * Limpia los filtros
   */
  clearFilters(): void {
    this.searchQuery = '';
    this.departmentFilter = '';
    this.statusFilter = 'all';
    this.applyFilters();
  }

  /**
   * Maneja el cambio de página
   */
  onPageChange(pageNumber: number): void {
    this.pageNumber = pageNumber;
    this.loadEmployees();
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  /**
   * Obtiene las iniciales del empleado
   */
  getInitials(employee: EmployeeListResponseDto): string {
    const firstName = employee.firstName || '';
    const lastName = employee.lastName || '';
    return (firstName.charAt(0) + lastName.charAt(0)).toUpperCase();
  }

  /**
   * Formatea la fecha de contratación
   */
  formatHireDate(hireDate: string | Date | undefined): string {
    if (!hireDate) return '-';

    const date = new Date(hireDate);
    return date.toLocaleDateString('es-ES', {
      day: '2-digit',
      month: 'short',
      year: 'numeric'
    });
  }

  /**
   * Calcula el tiempo desde la contratación
   */
  getTimeEmployed(hireDate: string | Date | undefined): string {
    if (!hireDate) return '-';

    const hire = new Date(hireDate);
    const now = new Date();
    const diffMs = now.getTime() - hire.getTime();

    if (diffMs < 0) return '-';

    const diffMonths = Math.floor(diffMs / (1000 * 60 * 60 * 24 * 30));

    if (diffMonths < 1) {
      return 'Menos de 1 mes';
    }

    if (diffMonths < 12) {
      return `${diffMonths} ${diffMonths === 1 ? 'mes' : 'meses'}`;
    } else {
      const years = Math.floor(diffMonths / 12);
      return `${years} ${years === 1 ? 'año' : 'años'}`;
    }
  }

  /**
   * Obtiene la clase de estado
   */
  getStatusClass(employee: EmployeeListResponseDto): string {
    return employee.enabled ? 'status-active' : 'status-inactive';
  }

  /**
   * Obtiene el texto de estado
   */
  getStatusText(employee: EmployeeListResponseDto): string {
    return employee.enabled ? 'Activo' : 'Inactivo';
  }

  /**
   * Navega al detalle del empleado
   */
  viewEmployee(employee: EmployeeListResponseDto): void {
    this.router.navigate(['/home/employee', employee.idEmployee]);
  }

  /**
   * Navega a la edición del empleado
   */
  editEmployee(employee: EmployeeListResponseDto): void {
    this.router.navigate(['/home/employee', employee.idEmployee, 'edit']);
  }

  /**
   *  Desactiva/activa un empleado con confirmación
   */
  async toggleEmployeeStatus(employee: EmployeeListResponseDto): Promise<void> {
    const action = employee.enabled ? 'desactivar' : 'activar';
    
    const confirmed = await this.confirmDialogService.confirm({
      header: `${action.charAt(0).toUpperCase() + action.slice(1)} Empleado`,
      message: `¿Estás seguro de ${action} a ${employee.firstName} ${employee.lastName}?`,
      acceptLabel: `Sí, ${action}`,
      rejectLabel: 'Cancelar'
    });
    
    if (confirmed) {
      this.employeeService.toggleEmployeeStatus(employee.idEmployee!).subscribe({
        next: (response) => {
          employee.enabled = response.active;
          this.calculateStats();
          
          this.toastService.success(
            'Estado actualizado',
            `El empleado ha sido ${action}do correctamente`
          );
        },
        error: (error) => {
          console.error('Error al cambiar estado:', error);
          
          this.toastService.error(
            'Error al actualizar',
            error.error?.message || 'No se pudo cambiar el estado del empleado'
          );
        }
      });
    }
  }

  /**
   *  Elimina un empleado con confirmación
   */
  async deleteEmployee(employee: EmployeeListResponseDto): Promise<void> {
    const confirmed = await this.confirmDialogService.confirm({
      header: 'Eliminar Empleado',
      message: `¿Estás seguro de eliminar a ${employee.firstName} ${employee.lastName}? Esta acción no se puede deshacer.`,
      acceptLabel: 'Sí, eliminar',
      rejectLabel: 'Cancelar'
    });

    if (confirmed) {
      // TODO: Implementar llamada al backend
      // this.employeeService.deleteEmployee(employee.idEmployee).subscribe(...)

      this.toastService.success(
        'Empleado eliminado',
        'El empleado ha sido eliminado correctamente'
      );

      this.loadEmployees();
    }
  }

  /**
   * Navega a crear nuevo empleado
   */
  createEmployee(): void {
    this.router.navigate(['/home/employee/new']);
  }

  getRecordsRange(): string {
    const start = this.pageNumber * this.rowsPerPage + 1;
    const end = Math.min((this.pageNumber + 1) * this.rowsPerPage, this.totalElements);
    return `${start}-${end} de ${this.totalElements}`;
  }

  onPageSizeChange(event: Event): void {
    const select = event.target as HTMLSelectElement;
    this.rowsPerPage = parseInt(select.value, 10);
    this.pageNumber = 0;
    this.loadEmployees();
  }

  getTotalPages(): number {
    return Math.ceil(this.totalElements / this.rowsPerPage);
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