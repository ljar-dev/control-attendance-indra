import { Component, OnInit, OnDestroy } from '@angular/core';
import { Chart, ChartConfiguration, registerables } from 'chart.js';
import { ReportControllerService } from 'src/app/generated/api';

// Registrar todos los componentes de Chart.js
Chart.register(...registerables);

interface AttendanceStats {
  onTime: number;
  late: number;
  absent: number;
  earlyDeparture: number;
  onTimePercentage: number;
  latePercentage: number;
  absentPercentage: number;
  earlyDeparturePercentage: number;
}

interface JustificationStats {
  totalRequired: number;
  justified: number;
  notJustified: number;
  justifiedPercentage: number;
  notJustifiedPercentage: number;
}

interface GeneralReport {
  attendanceStats: AttendanceStats;
  justificationStats: JustificationStats;
  startDate: string;
  endDate: string;
  totalEmployees: number;
  totalRecords: number;
}

@Component({
  selector: 'app-attendance-chart-report',
  templateUrl: './attendance-chart-report.component.html',
  styleUrls: ['./attendance-chart-report.component.css']
})
export class AttendanceChartReportComponent {

  // Datos
  reportData: GeneralReport | null = null;
  loading: boolean = false;

  // Filtros de fecha
  dateFrom: string = '';
  dateTo: string = '';

  // Charts
  attendanceChart: Chart | null = null;
  justificationChart: Chart | null = null;

  constructor(
    private reportService: ReportControllerService
  ) { }

  ngOnInit(): void {
    this.initializeDateFilters();
    this.loadReport();
  }

  ngOnDestroy(): void {
    // Destruir charts al salir del componente
    this.destroyCharts();
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
   * Formatea fecha para input type="date"
   */
  formatDateForInput(date: Date): string {
    const year = date.getFullYear();
    const month = (date.getMonth() + 1).toString().padStart(2, '0');
    const day = date.getDate().toString().padStart(2, '0');
    return `${year}-${month}-${day}`;
  }

  /**
   * Carga el reporte desde el backend
   */
  loadReport(): void {
    if (!this.dateFrom || !this.dateTo) {
      alert('Por favor selecciona un rango de fechas válido');
      return;
    }

    this.loading = true;

    this.reportService.getGeneralReport(this.dateFrom, this.dateTo).subscribe({
      next: (response: any) => {
        console.log('Reporte recibido:', response);

        //  VALIDAR QUE LA RESPUESTA TENGA LA ESTRUCTURA CORRECTA
        if (response && response.attendanceStats && response.justificationStats) {
          this.reportData = response;

          // Esperar un momento para que el DOM se actualice
          setTimeout(() => {
            this.createCharts();
          }, 100);
        } else {
          console.error('Respuesta del servidor inválida:', response);
          alert('La respuesta del servidor no tiene el formato esperado');
          this.reportData = null;
        }

        this.loading = false;
      },
      error: (error) => {
        console.error('Error al cargar reporte:', error);
        this.loading = false;
        this.reportData = null;

        if (error.status === 401) {
          alert('Sesión expirada. Por favor inicia sesión nuevamente.');
        } else if (error.status === 403) {
          alert('No tienes permisos para ver este reporte.');
        } else if (error.status === 500) {
          alert('Error en el servidor. Verifica que haya datos en el rango de fechas seleccionado.');
        } else {
          alert('Error al cargar el reporte. Intenta nuevamente.');
        }
      }
    });
  }

  /**
   * Aplica los filtros y recarga el reporte
   */
  applyFilters(): void {
    this.destroyCharts();
    this.loadReport();
  }

  /**
   * Limpia los filtros
   */
  clearFilters(): void {
    this.initializeDateFilters();
    this.applyFilters();
  }

  /**
   * Crea las gráficas
   */
  createCharts(): void {
    if (!this.reportData) return;

    this.createAttendanceChart();
    this.createJustificationChart();
  }

  /**
   * Destruye las gráficas existentes
   */
  destroyCharts(): void {
    if (this.attendanceChart) {
      this.attendanceChart.destroy();
      this.attendanceChart = null;
    }
    if (this.justificationChart) {
      this.justificationChart.destroy();
      this.justificationChart = null;
    }
  }

  /**
 * Crea el gráfico de asistencias
 */
  createAttendanceChart(): void {
    //  VALIDAR QUE EXISTAN LOS DATOS
    if (!this.reportData || !this.reportData.attendanceStats) {
      console.warn('No hay datos de asistencia para graficar');
      return;
    }

    const canvas = document.getElementById('attendanceChart') as HTMLCanvasElement;
    if (!canvas) {
      console.error('Canvas attendanceChart no encontrado');
      return;
    }

    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    const stats = this.reportData.attendanceStats;

    //  VALIDAR QUE LOS VALORES NO SEAN UNDEFINED
    const onTime = stats.onTime || 0;
    const late = stats.late || 0;
    const absent = stats.absent || 0;
    const earlyDeparture = stats.earlyDeparture || 0;

    const config: ChartConfiguration = {
      type: 'doughnut',
      data: {
        labels: ['Puntuales', 'Tarde', 'Ausencias', 'Salida Temprana'],
        datasets: [{
          label: 'Asistencias',
          data: [onTime, late, absent, earlyDeparture],
          backgroundColor: [
            '#10b981', // Verde - Puntuales
            '#f59e0b', // Amarillo - Tarde
            '#ef4444', // Rojo - Ausencias
            '#f97316'  // Naranja - Salida temprana
          ],
          borderWidth: 2,
          borderColor: '#ffffff'
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: {
            position: 'bottom',
            labels: {
              padding: 15,
              font: {
                size: 12,
                family: "'Inter', sans-serif"
              }
            }
          },
          tooltip: {
            callbacks: {
              label: (context) => {
                const label = context.label || '';
                const value = context.parsed || 0;
                const percentage = this.getPercentage(context.dataIndex);
                return `${label}: ${value} (${percentage}%)`;
              }
            }
          }
        }
      }
    };

    this.attendanceChart = new Chart(ctx, config);
  }

  /**
 * Crea el gráfico de justificaciones
 */
  createJustificationChart(): void {
    //  VALIDAR QUE EXISTAN LOS DATOS
    if (!this.reportData || !this.reportData.justificationStats) {
      console.warn('No hay datos de justificación para graficar');
      return;
    }

    const canvas = document.getElementById('justificationChart') as HTMLCanvasElement;
    if (!canvas) {
      console.error('Canvas justificationChart no encontrado');
      return;
    }

    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    const stats = this.reportData.justificationStats;

    //  VALIDAR QUE LOS VALORES NO SEAN UNDEFINED
    const justified = stats.justified || 0;
    const notJustified = stats.notJustified || 0;

    const config: ChartConfiguration = {
      type: 'pie',
      data: {
        labels: ['Justificadas', 'Pendientes'],
        datasets: [{
          label: 'Justificaciones',
          data: [justified, notJustified],
          backgroundColor: [
            '#3b82f6', // Azul - Justificadas
            '#94a3b8'  // Gris - Pendientes
          ],
          borderWidth: 2,
          borderColor: '#ffffff'
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: {
            position: 'bottom',
            labels: {
              padding: 15,
              font: {
                size: 12,
                family: "'Inter', sans-serif"
              }
            }
          },
          tooltip: {
            callbacks: {
              label: (context) => {
                const label = context.label || '';
                const value = context.parsed || 0;
                const percentage = this.getJustificationPercentage(context.dataIndex);
                return `${label}: ${value} (${percentage}%)`;
              }
            }
          }
        }
      }
    };

    this.justificationChart = new Chart(ctx, config);
  }


  /**
   * Obtiene el porcentaje de asistencia según el índice
   */
  getPercentage(index: number): number {
    if (!this.reportData) return 0;

    const stats = this.reportData.attendanceStats;
    const percentages = [
      stats.onTimePercentage,
      stats.latePercentage,
      stats.absentPercentage,
      stats.earlyDeparturePercentage
    ];

    return percentages[index] || 0;
  }

  /**
   * Obtiene el porcentaje de justificación según el índice
   */
  getJustificationPercentage(index: number): number {
    if (!this.reportData) return 0;

    const stats = this.reportData.justificationStats;
    const percentages = [
      stats.justifiedPercentage,
      stats.notJustifiedPercentage
    ];

    return percentages[index] || 0;
  }

  /**
 * Formatea fecha para mostrar
 */
  formatDateDisplay(dateStr: string | undefined): string {
    if (!dateStr) return '-';

    try {
      const date = new Date(dateStr);
      return date.toLocaleDateString('es-ES', {
        day: '2-digit',
        month: 'long',
        year: 'numeric'
      });
    } catch (error) {
      return '-';
    }
  }
}