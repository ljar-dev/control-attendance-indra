import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, FormArray, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { EmployeeControllerService, EmployeeRequestDto } from 'src/app/generated/api';
import { ConfirmDialogService } from 'src/app/modules/shared/services/confirm-dialog/confirm-dialog.service';
import { ToastService } from 'src/app/modules/shared/services/toast/toast.service';

interface DayOfWeekOption {
  label: string;
  value: string;
}

@Component({
  selector: 'app-employee-add-edit',
  templateUrl: './employee-add-edit.component.html',
  styleUrls: ['./employee-add-edit.component.css']
})
export class EmployeeAddEditComponent implements OnInit {
  
  employeeForm!: FormGroup;
  isEditMode: boolean = false;
  employeeId: number | null = null;
  loading: boolean = false;
  submitting: boolean = false;

  daysOfWeek: DayOfWeekOption[] = [
    { label: 'Lunes', value: 'MONDAY' },
    { label: 'Martes', value: 'TUESDAY' },
    { label: 'Miércoles', value: 'WEDNESDAY' },
    { label: 'Jueves', value: 'THURSDAY' },
    { label: 'Viernes', value: 'FRIDAY' },
    { label: 'Sábado', value: 'SATURDAY' },
    { label: 'Domingo', value: 'SUNDAY' }
  ];

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private employeeService: EmployeeControllerService,
    private confirmDialogService: ConfirmDialogService,
    private toastService: ToastService
  ) {}

  ngOnInit(): void {
    this.initForm();
    this.checkEditMode();
  }

  initForm(): void {
    this.employeeForm = this.fb.group({
      firstName: ['', [Validators.required, Validators.minLength(2)]],
      lastName: ['', [Validators.required, Validators.minLength(2)]],
      department: ['', Validators.required],
      position: ['', Validators.required],
      hireDate: ['', Validators.required],
      enabled: [true],
      workSchedules: this.fb.array([], this.duplicateDayValidator())
    });

    this.addWorkSchedule();
  }

  duplicateDayValidator() {
    return (formArray: AbstractControl): ValidationErrors | null => {
      const array = formArray as FormArray;
      const days = array.controls.map(control => control.get('dayOfWeek')?.value);
      const hasDuplicates = days.some((day, index) => days.indexOf(day) !== index);
      return hasDuplicates ? { duplicateDay: true } : null;
    };
  }

  checkEditMode(): void {
    const id = this.route.snapshot.paramMap.get('id');
    
    if (id && id !== 'new') {
      this.isEditMode = true;
      this.employeeId = parseInt(id, 10);
      this.loadEmployee(this.employeeId);
    }
  }

  loadEmployee(id: number): void {
    this.loading = true;

    this.employeeService.getEmployeeById(id).subscribe({
      next: (employee) => {
        this.employeeForm.patchValue({
          firstName: employee.firstName,
          lastName: employee.lastName,
          department: employee.department,
          position: employee.position,
          hireDate: this.formatDateForInput(employee.hireDate),
          //enabled: employee.enabled
        });

        this.clearWorkSchedules();
        if (employee.workSchedules && employee.workSchedules.length > 0) {
          employee.workSchedules.forEach(schedule => {
            this.addWorkScheduleWithData(schedule);
          });
        } else {
          this.addWorkSchedule();
        }

        this.loading = false;

        this.toastService.success(
          'Datos cargados',
          `Editando empleado: ${employee.firstName} ${employee.lastName}`
        );
      },
      error: (error) => {
        console.error('Error al cargar empleado:', error);
        this.loading = false;

        this.toastService.error(
          'Error al cargar',
          'No se pudieron cargar los datos del empleado'
        );

        this.router.navigate(['/home/employee/list']);
      }
    });
  }

  formatDateForInput(date: string | Date | undefined): string {
    if (!date) return '';
    
    const d = new Date(date);
    const year = d.getFullYear();
    const month = (d.getMonth() + 1).toString().padStart(2, '0');
    const day = d.getDate().toString().padStart(2, '0');
    
    return `${year}-${month}-${day}`;
  }

  get workSchedules(): FormArray {
    return this.employeeForm.get('workSchedules') as FormArray;
  }

  createWorkScheduleGroup(data?: any): FormGroup {
    return this.fb.group({
      dayOfWeek: [data?.dayOfWeek || 'MONDAY', Validators.required],
      startTime: [data?.startTime || '08:00', Validators.required],
      endTime: [data?.endTime || '17:00', Validators.required],
      enabled: [data?.enabled !== undefined ? data.enabled : true]
    });
  }

  addWorkSchedule(): void {
    this.workSchedules.push(this.createWorkScheduleGroup());
  }

  addWorkScheduleWithData(data: any): void {
    const formattedData = {
      dayOfWeek: data.dayOfWeek,
      startTime: this.formatTimeForInput(data.startTime),
      endTime: this.formatTimeForInput(data.endTime),
      enabled: data.enabled
    };
    
    this.workSchedules.push(this.createWorkScheduleGroup(formattedData));
  }

  formatTimeForInput(time: any): string {
    if (!time) return '08:00';
    
    if (typeof time === 'string') {
      return time.substring(0, 5);
    }
    
    if (time.hour !== undefined && time.minute !== undefined) {
      const hour = time.hour.toString().padStart(2, '0');
      const minute = time.minute.toString().padStart(2, '0');
      return `${hour}:${minute}`;
    }
    
    return '08:00';
  }

  async removeWorkSchedule(index: number): Promise<void> {
    if (this.workSchedules.length <= 1) {
      this.toastService.warning(
        'Acción no permitida',
        'Debe haber al menos un horario de trabajo'
      );
      return;
    }

    const confirmed = await this.confirmDialogService.confirm({
      header: 'Eliminar Horario',
      message: '¿Estás seguro de que deseas eliminar este horario?',
      acceptLabel: 'Sí, eliminar',
      rejectLabel: 'Cancelar'
    });

    if (confirmed) {
      this.workSchedules.removeAt(index);

      this.toastService.success(
        'Horario eliminado',
        'El horario ha sido eliminado correctamente'
      );
    }
  }

  clearWorkSchedules(): void {
    while (this.workSchedules.length > 0) {
      this.workSchedules.removeAt(0);
    }
  }

  hasError(fieldName: string): boolean {
    const field = this.employeeForm.get(fieldName);
    return !!(field && field.invalid && (field.dirty || field.touched));
  }

  getErrorMessage(fieldName: string): string {
    const field = this.employeeForm.get(fieldName);
    
    if (field?.hasError('required')) {
      return 'Este campo es requerido';
    }
    
    if (field?.hasError('minlength')) {
      const minLength = field.errors?.['minlength'].requiredLength;
      return `Debe tener al menos ${minLength} caracteres`;
    }
    
    return '';
  }

  parseTimeToBackend(timeString: string): any {
    const [hour, minute] = timeString.split(':').map(Number);
    
    return {
      hour: hour,
      minute: minute,
      second: 0,
      nano: 0
    };
  }

  prepareDataForBackend(): EmployeeRequestDto {
    const formValue = this.employeeForm.value;
    
    return {
      firstName: formValue.firstName.trim(),
      lastName: formValue.lastName.trim(),
      department: formValue.department.trim(),
      position: formValue.position.trim(),
      hireDate: formValue.hireDate,
      enabled: formValue.enabled,
      workSchedules: formValue.workSchedules.map((schedule: any) => ({
        dayOfWeek: schedule.dayOfWeek,
        startTime: this.parseTimeToBackend(schedule.startTime),
        endTime: this.parseTimeToBackend(schedule.endTime),
        enabled: schedule.enabled
      }))
    };
  }

  async onSubmit(): Promise<void> {
    if (this.employeeForm.invalid) {
      this.employeeForm.markAllAsTouched();

      if (this.workSchedules.errors?.['duplicateDay']) {
        this.toastService.error(
          'Días duplicados',
          'No puedes tener horarios con el mismo día de la semana'
        );
        return;
      }

      this.toastService.warning(
        'Formulario incompleto',
        'Por favor, completa todos los campos requeridos correctamente'
      );
      return;
    }

    const confirmed = await this.confirmDialogService.confirm({
      header: this.isEditMode ? 'Actualizar Empleado' : 'Crear Empleado',
      message: this.isEditMode 
        ? '¿Estás seguro de que deseas actualizar los datos de este empleado?' 
        : '¿Estás seguro de que deseas crear este nuevo empleado?',
      acceptLabel: this.isEditMode ? 'Sí, actualizar' : 'Sí, crear',
      rejectLabel: 'Cancelar'
    });

    if (!confirmed) {
      return;
    }

    this.submitting = true;
    const employeeData = this.prepareDataForBackend();

    if (this.isEditMode && this.employeeId) {
      this.employeeService.updateEmployee(this.employeeId, employeeData).subscribe({
        next: (response) => {
          this.toastService.success(
            'Empleado actualizado',
            `${employeeData.firstName} ${employeeData.lastName} ha sido actualizado correctamente`
          );

          setTimeout(() => {
            this.router.navigate(['/home/employee/list']);
          }, 500);
        },
        error: (error) => {
          console.error('Error al actualizar empleado:', error);
          this.submitting = false;

          this.toastService.error(
            'Error al actualizar',
            error.error?.message || 'No se pudo actualizar el empleado. Intenta nuevamente.'
          );
        }
      });
    } else {
      this.employeeService.createEmployee(employeeData).subscribe({
        next: (response) => {
          this.toastService.success(
            'Empleado creado',
            `${employeeData.firstName} ${employeeData.lastName} ha sido registrado correctamente`
          );

          setTimeout(() => {
            this.router.navigate(['/home/employee/list']);
          }, 500);
        },
        error: (error) => {
          console.error('Error al crear empleado:', error);
          this.submitting = false;

          if (error.status === 409) {
            this.toastService.error(
              'Empleado duplicado',
              'Ya existe un empleado con estos datos'
            );
          } else {
            this.toastService.error(
              'Error al crear',
              error.error?.message || 'No se pudo crear el empleado. Intenta nuevamente.'
            );
          }
        }
      });
    }
  }

  async onCancel(): Promise<void> {
    const confirmed = await this.confirmDialogService.confirm({
      header: 'Cancelar',
      message: '¿Estás seguro de que deseas cancelar? Los cambios no guardados se perderán.',
      acceptLabel: 'Sí, cancelar',
      rejectLabel: 'No, continuar editando'
    });

    if (confirmed) {
      this.toastService.info(
        'Operación cancelada',
        'No se guardaron los cambios'
      );

      this.router.navigate(['/home/employee/list']);
    }
  }
}