package com.indra.attendance_control.services.interfaces;

import java.time.LocalDate;

import com.indra.attendance_control.dtos.out.AttendanceGeneralReportDto;

public interface IReportService {
    /**
     * Genera reporte general de asistencias en un rango de fechas
     */
    AttendanceGeneralReportDto generateGeneralReport(LocalDate startDate, LocalDate endDate);
}
