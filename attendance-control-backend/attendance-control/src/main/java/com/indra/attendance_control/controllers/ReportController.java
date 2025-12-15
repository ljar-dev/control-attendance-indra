package com.indra.attendance_control.controllers;

import com.indra.attendance_control.dtos.out.AttendanceGeneralReportDto;
import com.indra.attendance_control.services.interfaces.IReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping(value ="/api/reports", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class ReportController {
    
    private final IReportService reportService;

    /**
     * Genera reporte general de asistencias
     * GET /api/reports/general?startDate=2024-01-01&endDate=2024-01-31
     */
    @GetMapping("/general")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AttendanceGeneralReportDto> getGeneralReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        // Validar que startDate sea anterior o igual a endDate
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("La fecha de inicio no puede ser posterior a la fecha de fin");
        }
        
        AttendanceGeneralReportDto report = reportService.generateGeneralReport(startDate, endDate);
        
        return ResponseEntity.ok(report);
    }
}